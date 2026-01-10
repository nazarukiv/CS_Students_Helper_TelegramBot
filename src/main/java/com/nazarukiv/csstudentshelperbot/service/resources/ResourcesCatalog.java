package com.nazarukiv.csstudentshelperbot.service.resources;

import java.util.*;

public class ResourcesCatalog {

    private final Map<ResourceCategory, List<ResourceItem>> data = new EnumMap<>(ResourceCategory.class);

    public ResourcesCatalog() {
        data.put(ResourceCategory.IDE, List.of(
                new ResourceItem("IntelliJ IDEA", "https://www.jetbrains.com/idea/", "Best for Java"),
                new ResourceItem("VS Code", "https://code.visualstudio.com/", "Lightweight editor")
        ));

        data.put(ResourceCategory.GIT, List.of(
                new ResourceItem("Git Book", "https://git-scm.com/book/en/v2", "Free official book"),
                new ResourceItem("GitHub Docs", "https://docs.github.com/", "How GitHub works")
        ));
    }

    public List<ResourceItem> get(ResourceCategory category) {
        return data.getOrDefault(category, List.of());
    }
}