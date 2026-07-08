package com.example.distributed_lovable_clone.common_lib.enums;

public enum ChatEventType {
    THOUGHT, //thought for 2s
    MESSAGE, //standard
    FILE_EDIT, //code generation <file>
    TOOL_LOG //reading file ... <tool>
}
