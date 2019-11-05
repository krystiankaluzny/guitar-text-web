package org.guitartext;

import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;
import com.google.common.base.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
class FileService {
    private static final Logger LOGGER = LoggerFactory.getLogger(FileService.class);
    private static final String CACHE = "children";

    @Cacheable(value = CACHE, key = "#parentId")
    public List<FileDTO> getChildren(final Drive service, final String parentId) throws IOException {

        LOGGER.info("children");

        final String id = Strings.isNullOrEmpty(parentId) ? "root" : parentId;

        final FileList fileList = service.files().list()
                .setQ(String.format("\"%s\" in parents", id))
                .setFields("files(id, name, parents, mimeType)")
                .setOrderBy("name")
                .execute();

        final List<File> files = fileList.getFiles();
        final List<FileDTO> result;

        if (files == null || files.isEmpty()) {
            result = Collections.emptyList();
        } else {
            result = new ArrayList<>(files.size());

            for (final File file : files) {
                result.add(new FileDTO(
                        file.getId(),
                        file.getName(),
                        file.getMimeType()));
            }
        }

        return result;
    }

    @CacheEvict(value = CACHE, key = "#parentId")
    public void clearInfo(final String parentId) {}
}
