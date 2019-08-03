/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.com.tecnicon.controller;

import br.com.tecnicon.server.context.TClassLoader;
import br.com.tecnicon.server.execoes.ExcecaoTecnicon;
import br.com.tecnicon.server.sessao.TVariavelSessao;
import br.com.tecnicon.server.sessao.VariavelSessao;
import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 *
 * @author joao.oliveira
 */
@WebServlet(name = "EmailService", urlPatterns =
{
    "/EmailService"
})
public class EmailService extends HttpServlet
{

    // <editor-fold defaultstate="collapsed" desc="HttpServlet methods. Click on the + sign on the left to edit the code.">
    /**
     * Handles the HTTP <code>GET</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException
    {
        doPost(request, response);
    }

    /**
     * Handles the HTTP <code>POST</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException
    {
        
        String ejb = request.getParameter("ejb");
        String classe = request.getParameter("classe");
        String metodo = request.getParameter("metodo");
        String parametro = request.getParameter("parametro");

        VariavelSessao vs = new TVariavelSessao();
        vs.addParametros("empresa", request.getParameter("empresa"));
        vs.addParametros("senha", "");
        vs.addParametros("nome", "SYSDBA");
        vs.addParametros("usuario", "SYSDBA");
        vs.addParametros("sessao", "-9876");
        vs.addParametros("parametro", parametro);
     
        String method = "";
        try
        {
            method = (String) TClassLoader.execMethod(ejb + "/" + classe, metodo, vs);
        } catch (ExcecaoTecnicon ex)
        {
        }
    }

    /**
     * Returns a short description of the servlet.
     *
     * @return a String containing servlet description
     */
    @Override
    public String getServletInfo()
    {
        return "Short description";
    }// </editor-fold>

}
