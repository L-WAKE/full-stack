package com.example.house.modules.system;

import java.util.List;

public record MenuItem(
    String name,
    String title,
    String path,
    String icon,
    String permissionCode,
    List<MenuItem> children
) {
}
