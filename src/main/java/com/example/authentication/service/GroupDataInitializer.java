package com.example.authentication.service;

import com.example.authentication.entity.Group;
import com.example.authentication.repository.GroupRepository;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class GroupDataInitializer implements CommandLineRunner {

    private final GroupRepository groupRepository;

    public GroupDataInitializer(GroupRepository groupRepository) {
        this.groupRepository = groupRepository;
    }

    @Override
    public void run(String... args) {

        List<String> technologies = List.of(
            "Java",
            "Python",
            "React",
            "Angular",
            "Spring Boot"
        );

        for (String technology : technologies) {

            if (!groupRepository.existsByNameIgnoreCase(technology)) {

                Group group = new Group(technology);

                groupRepository.save(group);
            }
        }

        System.out.println("Technology groups initialized!");
    }
}