/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.com.tecnicon.controller;

import java.util.HashMap;

/**
 *
 * @author erli.balbinot
 */
public class ValidaParametro
{

    private static ValidaParametro instance;
    private HashMap<String, String> chats = new HashMap<>();

    public static ValidaParametro getInstance()
    {

        if (instance == null)
        {
            instance = new ValidaParametro();
        }

        return instance;
    }

    public String getChat(String usuario)
    {
        return chats.get(usuario);
    }

    public void setChat(String usuario, String chat)
    {
        chats.put(usuario, chat);
    }

}
