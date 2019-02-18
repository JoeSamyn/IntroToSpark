package com.joesamyn.courses.model;

import java.util.ArrayList;
import java.util.List;

public class SimpleCourseIdeaDAOImp implements CourseIdeaDAO {
    // By implementing this interface, we are saying this object must implement the methods in the interface.

    // this is our temporary database, in this case we are using a list just to simulate data flow.

    private List<CourseIdea> ideas;

    public SimpleCourseIdeaDAOImp(){

        // Simply instantiating our new temp database.
        ideas = new ArrayList<>();
    }

    // This will allow data to be added to the list/database
    @Override
    public boolean add(CourseIdea idea) {
        return ideas.add(idea);
    }

    // this will return a copy of the ideas list
    // We return a copy of the list so the original list/data cannot be accidentally modified.
    @Override
    public List<CourseIdea> findAll() {
        // collections can take another collections as an argument, and then generates a new collection from the argument given.
        // We are returning an arraylist which is a copy of ideas.
        return new ArrayList<>(ideas);
    }

    @Override
    public CourseIdea findBySlug(String slug) {
        // This is a stream, very clean way of searching through a list.
        // It firsts checks to see if the ideas slug matches the slug passed in.
        // If it does, then it returns that slug, if it does not find it then it throws this class not found exception.
        return  ideas.stream()
                .filter(idea -> idea.getSlug().equals(slug))
                .findFirst()
                .orElseThrow(NotFoundException::new);
    }
}
