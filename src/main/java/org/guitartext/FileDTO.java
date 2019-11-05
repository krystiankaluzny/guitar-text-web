package org.guitartext;

import lombok.Value;

import java.util.List;

@Value
public class FileDTO {
    private final String id;
    private final String name;
    private final String mimeType;
}
