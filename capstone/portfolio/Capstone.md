# Section 1: Program

Please include a zip file of the final repository in this directory.

# Section 2: Teamwork

For the capstone project, our team seperately used test driven development to develop seperate recursive descent parsers. We wrote tests to assist each other in writing a fully complete parser. Both partners also wrote section four for the other partner's project. 
# Section 3: Design pattern

The Memoization Pattern is used to memoize calls to ```CatScriptType#getListType()```. If we are creating a list we don't want to store the type of each element with the element itself, so when the CatScriptType is shown a list it stores the type as a ListType() in the class instead. This speeds up algorithms on the list because we don't have to check each value's type, instead we used the pattern. So when a list is created it is created with the getListType() call instead.

<div style="background-color: #FFFF00">

public static CatscriptType getListType(CatscriptType type) {
        return new ListType(type);
    }

public Class getJavaType() {
        return javaClass;
    }

public static class ListType extends CatscriptType {
    private final CatscriptType componentType;
    public ListType(CatscriptType componentType) {
        super("list<" + componentType.toString() + ">", List.class);
        this.componentType = componentType;
        }

</div>

# Section 4: Technical writing. Include the technical document that accompanied your capstone project.

# Section 5: UML. 

Include a UML diagram for parse elements

# Section 6: Design trade-offs

To be discussed later in the class

# Section 7: Software development life cycle model

Describe the model that you used to develop your capstone project. How did this model help and/or hinder your team?

We are using Test Driven Development (TDD) for this project