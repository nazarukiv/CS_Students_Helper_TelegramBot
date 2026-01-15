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

        data.put(ResourceCategory.DATABASES, List.of(
                new ResourceItem("w3schools SQL tutorial", "https://www.w3schools.com/sql/", "Free online tutorial"),
                new ResourceItem("SQL roadmap", "https://roadmap.sh/sql", "SQL roadmap")
        ));

        data.put(ResourceCategory.ALGORITHMS, List.of(
                new ResourceItem("Data Structures & Algorithms roadmap", "https://roadmap.sh/datastructures-and-algorithms", "algos and ds roadmap")
        ));

        data.put(ResourceCategory.AI_TOOLS, List.of(
                new ResourceItem("Chat GPT by OpenAI", "https://chatgpt.com/", "most popular AI tool")
        ));


        data.put(ResourceCategory.JAVA, List.of(
                new ResourceItem("Java Developer Roadmap", "https://roadmap.sh/java", "java roadmap")
        ));

        data.put(ResourceCategory.ROADMAPS, List.of(
                new ResourceItem("All roadmaps", "https://roadmap.sh/", "all roadmaps")
        ));





    }

    public List<ResourceItem> get(ResourceCategory category) {
        return data.getOrDefault(category, List.of());
    }
}