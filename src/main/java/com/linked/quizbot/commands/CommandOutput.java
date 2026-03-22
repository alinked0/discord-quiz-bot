package com.linked.quizbot.commands;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class CommandOutput extends ArrayList<Output> {
    
    public CommandOutput(){
        super();
	}
    public CommandOutput(Output out){
        super(List.of(out));
	}
    public CommandOutput(Collection<? extends Output> c){
        super(c);
	}
    @Override
	public String toString(){
		String res = "";
        for (Output out : this){
            res +=out.getAsText();
        }
        return res;
	}
}
