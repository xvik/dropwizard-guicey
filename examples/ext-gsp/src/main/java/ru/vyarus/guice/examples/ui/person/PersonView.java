package ru.vyarus.guice.examples.ui.person;

import ru.vyarus.guice.examples.model.Person;
import ru.vyarus.guicey.gsp.views.template.TemplateView;

/**
 * @author Vyacheslav Rusakov
 * @since 23.10.2019
 */
public class PersonView extends TemplateView {
    private final Person person;

    public PersonView(Person person) {
        super("person.ftl");
        this.person = person;
    }

    public Person getPerson() {
        return person;
    }
}