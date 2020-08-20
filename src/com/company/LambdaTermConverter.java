package com.company;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.*;

public class LambdaTermConverter {
    public static final String MENU_OPTIONS = "Enter:\n0 - to convert lambda term with names to nameless" + "\n1 - to convert nameless term to named" + "\n2 - to exit";
    public static final int ABSTRACTION_SUB_TERM_LENGTH = 5;
    public static final int NAMELESS_TERM_ABSTRACTION_SUBTERM_LENGTH = 2;
    
    //map the index of bounding lambda to variable
    private Map<Integer, Character> boundVariables;
    private Map<Integer, Character> freeVariables;
    
    public LambdaTermConverter() {
	this.boundVariables = new HashMap<>();
	this.freeVariables = new HashMap<>();
    }
    
    public static void main(String[] args) throws IOException {
	LambdaTermConverter lambdaTermConverter = new LambdaTermConverter();
	BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
	while(true) {
	    System.out.println(MENU_OPTIONS);
	    int choice = Integer.parseInt(reader.readLine());
	    
	    System.out.println("Enter lambda term:");
	    String term = reader.readLine();
	    
	    switch(choice) {
		case 0:
		    System.out.println(lambdaTermConverter.convertToNameless(term));
		    lambdaTermConverter.clear();
		    break;
		case 1:
		    System.out.println(lambdaTermConverter.addNamesTo(term));
		    lambdaTermConverter.clear();
		    break;
		case 2: {
		    reader.close();
		    return;
		}
	    }
	}
    }
    
    public String convertToNameless(String term) {
	StringBuilder namelessTerm = new StringBuilder();
	int currentLambdaIndex = 0;
	int abstractionsCount = 0;
	for(int i = 0; i<term.length(); i++) {
	    if(term.charAt(i)=='(') {
		namelessTerm.append('(');
	    }
	    if(term.charAt(i)==')') {
		--currentLambdaIndex;
		namelessTerm.append(')');
	    }
	    if(isVariable(term.charAt(i))) {
		if(!isAbstraction(term, i)) {
		    Character variable = term.charAt(i);
		    int variableDeBrujinIndex = getDeBrujinIndex(variable, currentLambdaIndex);
		    namelessTerm.append(variableDeBrujinIndex);
		} else {
		    ++currentLambdaIndex;
		    ++abstractionsCount;
		    Character boundVariable = getBoundVariableOfAbstraction(term, i);
		    boundVariables.put(abstractionsCount, boundVariable);
		    namelessTerm.append("lm");
		    i += ABSTRACTION_SUB_TERM_LENGTH - 1;
		}
	    }
	    
	}
	return namelessTerm.toString();
    }
    
    private void clear() {
	this.freeVariables.clear();
	this.boundVariables.clear();
    }
    
    private String addNamesTo(String term) {
	int abstractionsCount = 0;
	int currentLambdaIndex = 0;
	for(int i = 0; i<term.length(); i++) {
	    if(isNamelessAbstraction(term, i)) {
		++abstractionsCount;
		boundVariables.put(abstractionsCount, getFreshVariable());
		i += NAMELESS_TERM_ABSTRACTION_SUBTERM_LENGTH - 1;
	    }
	}
	
	StringBuilder namedTerm = new StringBuilder();
	for(int i = 0; i<term.length(); i++) {
	    if(term.charAt(i)=='(') {
		namedTerm.append('(');
	    }
	    if(term.charAt(i)==')') {
		--currentLambdaIndex;
		namedTerm.append(')');
	    }
	    if(isNamelessAbstraction(term, i)) {
		++currentLambdaIndex;
		namedTerm.append("lm " + boundVariables.get(currentLambdaIndex) + '.');
		i += NAMELESS_TERM_ABSTRACTION_SUBTERM_LENGTH - 1;
	    }
	    
	    if(isDeBrujinIndex(term, i)) {
		int deBrujinIndex = parseDeBrujinIndex(term, i);
		int variablesCorrespondingLambda = currentLambdaIndex - deBrujinIndex;
	 
		if(variablesCorrespondingLambda >= 0) {
		    Character variable = boundVariables.get(variablesCorrespondingLambda);
		    namedTerm.append(variable);
		} else {
		    variablesCorrespondingLambda *= (-1);
		    if(!freeVariables.containsKey(variablesCorrespondingLambda)) {
			freeVariables.put(variablesCorrespondingLambda, getFreshVariable());
		    }
		    namedTerm.append(freeVariables.get(variablesCorrespondingLambda));
		    i += Integer.toString(deBrujinIndex).length();
		}
	    }
	    
	}
	
	
	return namedTerm.toString();
    }
    
    private boolean isVariable(char c) {
	return c >= 'a' && c<='z';
    }
    
    private boolean isAbstraction(String term, int subTermStartIndex) {
	if(term.length() - subTermStartIndex<ABSTRACTION_SUB_TERM_LENGTH) {
	    return false;
	}
	String subTerm = term.substring(subTermStartIndex, subTermStartIndex + ABSTRACTION_SUB_TERM_LENGTH);
	return subTerm.startsWith("lm ") && subTerm.endsWith(".");
    }
    
    private int getDeBrujinIndex(Character variable, int currentLambdaIndex) {
	int variableDeBrujinIndex = 0;
	if(boundVariables.values().contains(variable)) {
	    int finalCurrentLambdaScopeIndex = currentLambdaIndex;
	    int variableBoundingLambdaIndex = boundVariables.entrySet()
							    .stream()
							    .filter(pair -> pair.getKey()<=finalCurrentLambdaScopeIndex && pair.getValue()
															       .equals(variable))
							    .max(Map.Entry.comparingByKey())
							    .get()
							    .getKey();
	    variableDeBrujinIndex = currentLambdaIndex - variableBoundingLambdaIndex;
	} else {
	    if(!freeVariables.values().contains(variable)) {
		freeVariables.put(freeVariables.size() + 1, variable);
	    }
	    for(Map.Entry<Integer, Character> e : freeVariables.entrySet()) {
		if(e.getValue()==variable) {
		    variableDeBrujinIndex = currentLambdaIndex + e.getKey();
		}
	    }
	}
	return variableDeBrujinIndex;
    }
    
    private Character getBoundVariableOfAbstraction(String term, int subTermStartIndex) {
	String subTerm = term.substring(subTermStartIndex, subTermStartIndex + ABSTRACTION_SUB_TERM_LENGTH);
	Character boundVariable = subTerm.charAt(3);
	
	return boundVariable;
    }
    
    private boolean isNamelessAbstraction(String term, int subTermStartIndex) {
	if(term.length() - subTermStartIndex<NAMELESS_TERM_ABSTRACTION_SUBTERM_LENGTH) {
	    return false;
	}
	return term.substring(subTermStartIndex).startsWith("lm");
    }
    
    private Character getFreshVariable() {
	for(int i = 'a'; i<='z'; i++) {
	    char c = (char) i;
	    if(!boundVariables.values().contains(c) && !freeVariables.values()
								     .contains(c)) {
		return c;
	    }
	}
	return '?';
    }
    
    private boolean isDeBrujinIndex(String term, int index) {
	return (term.charAt(index) >= '0' && term.charAt(index)<='9');
    }
    
    private int parseDeBrujinIndex(String term, int subtermStartIndex) {
	StringBuilder deBrujinIndex = new StringBuilder();
 
	int j = subtermStartIndex;
	while(j<term.length() && isDeBrujinIndex(term, j)) {
	    deBrujinIndex.append(term.charAt(j));
	    j++;
	}
 
	return Integer.parseInt(deBrujinIndex.toString());
    }
    
    private boolean isAbstractionInNamelessTerm(String term, int subTermStartIndex) {
	if(term.length() - subTermStartIndex<NAMELESS_TERM_ABSTRACTION_SUBTERM_LENGTH) {
	    return false;
	}
	String subTerm = term.substring(subTermStartIndex, subTermStartIndex + 2);
	return subTerm.startsWith("lm ") && subTerm.endsWith(".");
    }
    
}
