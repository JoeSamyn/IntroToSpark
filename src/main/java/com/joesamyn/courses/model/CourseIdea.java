package com.joesamyn.courses.model;

import com.github.slugify.Slugify;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

public class CourseIdea {

    private String slug;
    private String title;
    private String creator;
    // A set is simply an unordered collection of objects in which duplicate values cannot be stored.
    Set<String> voters;

    public CourseIdea(String title, String creator) {
        voters = new HashSet<>();
        this.title = title;
        this.creator = creator;
        // This slug allows us to make custom url id's, rather than using a number in the url we can now use the title,
        // i.e. /231 vs /idea1
        Slugify slg  = new Slugify();
        slug = slg.slugify(title);

    }

    // By not using setters here, we assure that the title and slug do not get out of sync.  If we had a setter for the title
    // and the slug, we would have to have a method that would check and make sure they are the same.
    public String getTitle() {
        return title;
    }

    public String getCreator() {
        return creator;
    }

    public String getSlug() { return slug; }

    public boolean AddVoter(String voterUsername){
        // returns true is the voter was added to the voters hashset.  Remember hashsets store unique objects, meaning there
        // cannot be duplicates.  Therefore if a voterUsername is tried to be added twice it will return false.
        return voters.add(voterUsername);
    }

    public int getVoteCount(){
        return voters.size();
    }

    public Set<String> getVoters(){
        return new HashSet<>(voters);
    }

    // we implement an equals object because what we believe makes 2 objects equal is different than what the standard method
    // thinks makes two objects equal.

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CourseIdea that = (CourseIdea) o;
        return Objects.equals(title, that.title) &&
                Objects.equals(creator, that.creator);
    }

    // We create our own custom hashcode so we can compare objects based on their fields therefor we need to override this method.
    // The standard method will randomly generate a hashcode every time, therefor making it very difficult to compare objects equality.
    @Override
    public int hashCode() {
        return Objects.hash(title, creator);
    }
}
