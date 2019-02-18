package com.joesamyn.courses;

import com.joesamyn.courses.model.CourseIdea;
import com.joesamyn.courses.model.CourseIdeaDAO;
import com.joesamyn.courses.model.NotFoundException;
import com.joesamyn.courses.model.SimpleCourseIdeaDAOImp;
import spark.ModelAndView;
import spark.Request;
import spark.template.handlebars.HandlebarsTemplateEngine;

import java.util.HashMap;
import java.util.Map;

import static spark.Spark.*;

public class Main {

    private static final String FLASH_MESSAGE_KEY = "flash_message";

    public static void main(String[] args) {

        staticFileLocation("/public");

        // Here the data access object is initialized
        // CourseIdeaDAO is our interface, and we are implementing the SimpleCourseDAOImp.
        // This could be changed later to implement a real database instead of SimpleCourseDAOImp.
        // If this was stored in a SimpleCOurseIdeaImp database then we could only use a SimpleCOurseIdeaImp database.
        // We store this in a DAO interface becasuse it allows us to implement any kind of database simply by changing the
        // implementation(SimpleCOurseIdeaImp)
        CourseIdeaDAO dao = new SimpleCourseIdeaDAOImp();

        // Before filters work in the way they are added, so the first before filter in the java file will run first.
        // This HTTP middleware can be used when you find yourself duplicating code and routes.

        // We do not put a path here so it checks this before method before every req and resp.  This is the most secure.
        before((req, resp) -> {
            // if the cookie "username" exists, an attribute is added to the request.
            Map<String, String> model = new HashMap<>();
            if (req.cookie("username") != null)
            {
                // This is the only place it looks for the username cookie now, every other spot it is going to look for
                // the username attribute that was created here.
                // Attributes can be used to store information that will be referred to multiple times in the codes through routes.
                req.attribute("username", req.cookie("username"));
                model.put("username", req.cookie("username"));
            }
            model.put("flashMessage", captureFlashMessage(req));
            req.attribute("model", model);
        });

        // We are using a filter here to catch users before they go to the ideas page.  This way we can make sure they
        // are logged in.  If they are not logged in, we can handle their request.  This is often referred to as http handling.
        before("/ideas", (req, resp) -> {
            // if the cookie "username" is equal to null, then we will not let them access the ideas page.
            if (req.attribute("username") == null)
            {
                setFlashMessage(req, "Whoops! Please sign in to view ideas.");

                resp.redirect("/");
                // this halt method is used to stop the request from hitting another route.  It is a way to prevent furthur
                // processing.
                halt();
            }
        });


        // Get is used to request data from a server.
        // Post is used to send information to a server.

        // Call static method get
        // with path /hello which is the URI
        // lambda takes 2 arguments req, res and returns hello world.  This is essentially just a function that returns hello world.
        // Get request is made looking for the URI /hello, and the function that follows will be called.
        // 0.0.0.0:4567 port is then 4567. localhost:4567/hello
        //get("/hello", (req, res) -> "Hello World");

        //When get request comes in with / run this route -> show index.hbs which is rendered using the Handlebars template engine.
        get("/", (req, resp) -> {
            //Map<String, String> model = new HashMap<>();
            // Requests the value of The username cookie which will be joesamyn
            // com.joesamyn.courses.model has key of "username" and value of username cookie
            return new ModelAndView(req.attribute("model"), "index.hbs");},
                new HandlebarsTemplateEngine());

        //Post method with dynamic text
        //When request of /sign-in is called, info from the form field is posted
        //A new map is created that has a key of username, and a value of the username entered by the current user, in this case joesamyn
        //
        post("/sign-in", (req, resp) -> {
            // A new key value pair for the username we are requesting from user.
            // This com.joesamyn.courses.model is where the applications data objects are stored, in this case the com.joesamyn.courses.model is the HashMap being used and the data is the username data.
            // Any kind of data structure can be pushed through to a com.joesamyn.courses.model, i.e. database, just used map in this case.
                Map<String, String> model = req.attribute("model");
                //Using variable for query parameter to make it more readable.
                String username = req.queryParams("username");
                // Sets cookies the first argument is the name you want to call the cookie
                // The second argument is the value that is given to that name.  In this case "username" is given the value of the username variable.
                // So username = joesamyn
                // Every request that is made to this domain will recieve this cookie data.
                resp.cookie("username", username);
                model.put("username", username);
                // This com.joesamyn.courses.model is put into the com.joesamyn.courses.model view and rendered by the template engine.
                // The view is what is visible on the website.
            resp.redirect("/");
            return null;
        });


        get("/ideas", (req, resp) -> {
            Map<String, Object> model = req.attribute("model");
            // we are using our data access object here to find all of the ideas that have been entered into our database.
            model.put("ideas", dao.findAll());
            // We are going to put this flash message into the map here when we show our ideas.
            // This will flash if it exists, otherwise it just won't show it.
            // This will show after a vote because we are redirecting to this page after our post request.
            // We will use capture instead of get so the message will disappear after being seen once.
            model.put("flashMessage", captureFlashMessage(req));
            return new ModelAndView(model, "ideas.hbs");},
                new HandlebarsTemplateEngine());

        post("/ideas", (req, resp) -> {
            // getting value for title in ideas.hbs form.
            String title = req.queryParams("title");
            // We create a new idea and provide it the title that was entered, and the username from the cookie

            CourseIdea idea = new CourseIdea(title, req.attribute("username"));
            // The idea is then added to the dao or database.
            dao.add(idea);
            // Here we cause a redirect that refreshes the page so the newly added idea will appear in the list.
            resp.redirect("/ideas");
            // We have to return something as required by the method, so standard pattern is just simply to return null.
            // This redirect calls the get request which refreshes our page. So since we are calling a get request that has
            // allready been established (above), we just return null and let that method handle it.
            return null;
        });

        // here :slug is pulling the value that is stored in the particular CourseIdea object.
        // :slug is the name of the parameter that we will want to refer to, this could be anything but we named the slug
        // parameter slug in out CourseIdeas object.
        post("/ideas/:slug/vote", (req, resp) -> {
            // This finds the idea object by the slug name.
            // This req.params finds the parameter named slug in the URL, so we are passing the value for slug into the
            // findBySlug() method.
            CourseIdea idea = dao.findBySlug(req.params("slug"));
            boolean added = idea.AddVoter(req.attribute("username"));
            // here we are setting our flash message value for when someone votes.
            if (added){ setFlashMessage(req, "Thanks for your vote!");}
            else{ setFlashMessage(req, "You already voted");}
            // We are adding a voter to the voter, and their name is pulled from the attribute that was created in the
            // before filter above.
            idea.AddVoter(req.attribute("username"));
            resp.redirect("/ideas");
            return null;
        });


        // We need to get the list of voters that have voted on the idea, which is the slug in the URL.

        get("/ideas/:slug/", (req, resp) -> {
            Map<String, Object> model = new HashMap<>();
            CourseIdea idea = dao.findBySlug(req.params("slug"));
            model.put("idea", idea);
            return new ModelAndView(model, "detail.hbs");
        }, new HandlebarsTemplateEngine());

        post("/ideas/:slug/", (req, resp) -> {
            CourseIdea idea = dao.findBySlug(req.params("slug"));
            idea.AddVoter(req.attribute("username"));
            resp.redirect("/ideas/:slug/vote");
            return null;
        });

        // Here we are catching our NotFoundException that is thrown if a slug is not found.
        // This exception takes the NotFoundException.class because we are going to handle this in the class we created.
        // then it takes and exception, req, and resp. The header on this page will be the 404 Error
        exception(NotFoundException.class, (exc, req, resp) -> {
            resp.status(404);
            // we can create our own rendering engine, which is what we are doing here.  All this will do is render a page, and
            // return html string. so we set a string equal to its return. In this case we are rendering a new page for our
            // not found error.
            HandlebarsTemplateEngine engine = new HandlebarsTemplateEngine();
            String html = engine.render(new ModelAndView(null, "not-found.hbs"));
            // this exception handler class does not have a return, therefor we have to set the body. So we set the body equal
            // to our rendered html string.
            resp.body(html);
        });
    }

    private static void setFlashMessage(Request req, String message) {
        // We do not want to create a session if we do not need to.  It will create a session by default.
        // We set the key value with an attribute
        req.session(true).attribute(FLASH_MESSAGE_KEY, message);
    }

    // This is just a static getter for the flash message constant.
    private static String getFlashMessage(Request req){
        // A session is a server side storage of information that is allowed to persist as long as the user is interacting
        // with the website. It creates a unique ID that is stored server side, and every time a request is made with this
        // unique ID, it retrieves the stored variables for use by other request pages.
        // if the session does not exist, return false
        // We put the false in the req.session because we do not want to check for a session and accidentally create one.
        // The false allows us to check but not create a new session.
        // If this doesn't exist then there is no flash message to get. Therefor no reason to keep going so we return null.
        if(req.session(false) == null){
            return null;
        }
        // If we know it exists, we look at all the attributes and see if any of them contain the flash message key.
        // If they do not contain it, then we pop out of the function by returning null.
        if(!req.session().attributes().contains(FLASH_MESSAGE_KEY)){
            return null;
        }

        // If it does exist we return it, but we need to cast it to a string because the key is a string, but the returned
        // value is an object. We pull out the attribute with the key of FLASH_MESSAGE_KEY
        return (String) req.session().attribute(FLASH_MESSAGE_KEY);
    }

    private static String captureFlashMessage(Request req) {
        // We get the message here and make sure it is not null
        String message = getFlashMessage(req);
        if (message != null){
            // if it is null we are going to remove the key for the flash_message_session.
            // This will allow the message to disappear after it is seen once.
            req.session().removeAttribute(FLASH_MESSAGE_KEY);
        }
        return message;
    }
}
