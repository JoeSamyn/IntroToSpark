package com.joesamyn.courses.model;

import java.util.List;

public interface CourseIdeaDAO {
    // This is where we define the standard operations that can be performed on the database.

    // Add new ideas to database
    boolean add(CourseIdea idea);

    // list the ideas that have been entered into database.
    List<CourseIdea> findAll();

    // Now a user can find a course idea by the slug that it has.
    CourseIdea findBySlug(String slug) throws ClassNotFoundException;
}
