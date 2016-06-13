package com.sam_chordas.android.stockhawk.rest;

/**
 * Created by erikllerena on 6/11/16.
 */
public class Utility {

    private String ErrorMsg;
    private String Error;

    public Utility(){
        super();
    }

   public void putError(String error)
    {

        this.Error=error;
    }

    public String getError()
    {
        return ErrorMsg;
    }
}
