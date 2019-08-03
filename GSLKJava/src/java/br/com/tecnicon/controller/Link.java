/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.com.tecnicon.controller;

import br.com.tecnicon.server.Propriedades;
import br.com.tecnicon.server.context.TecniconLookup;
import br.com.tecnicon.server.sessao.TVariavelSessao;
import br.com.tecnicon.server.sessao.VariavelSessao;
import java.io.IOException;
import java.io.PrintWriter;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 *
 * @author eduardo.wohlfahrt
 */
@WebServlet(name = "Link", urlPatterns = {"/Link"})
public class Link extends HttpServlet 
{
    /**
     * Processes requests for both HTTP <code>GET</code> and <code>POST</code>
     * methods.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException 
    {
        try (PrintWriter out = response.getWriter()) 
        {
            response.setContentType("text/html;charset=UTF-8");
            
            try
            {
                VariavelSessao vs = new TVariavelSessao();
                vs.addParametros("request", request);
                vs.addParametros("response", response);
                vs.addParametros("d", request.getParameter("d"));
                
                Object objeto = TecniconLookup.lookup("BannerTV", "ShowObjetoHTML");
                out.println((String) objeto.getClass().getMethod("getHTMLJS", VariavelSessao.class).invoke(objeto, vs));
            } catch (Exception | Error e)
            {
                Throwable cause = e;
                while (cause.getCause() != null)
                {
                    cause = cause.getCause();
                }
                
                retornaAviso(response, cause.getMessage());
            }
        }
    }
    
    public void retornaAviso(HttpServletResponse response, String msg) throws IOException
    {
        try (PrintWriter out = response.getWriter()) 
        {
            response.setContentType("text/html;charset=UTF-8");
            
            StringBuilder retorno = new StringBuilder();

            retorno.append("<!DOCTYPE html>");
            retorno.append("<html>");
            retorno.append("    <head>");
            retorno.append("        <title>TECNICON Business Suite</title>");            
            retorno.append("    </head>");
            retorno.append("    <body>");
            retorno.append("        <h1>Aviso</h1>");
            retorno.append("        <br><span>" + msg + "</span>");
            retorno.append("    </body>");
            retorno.append("</html>");

            out.println(retorno.toString());
        }
    }
    
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
            throws ServletException, IOException {
            processRequest(request, response);
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
            throws ServletException, IOException {
        processRequest(request, response);
    }

    /**
     * Returns a short description of the servlet.
     *
     * @return a String containing servlet description
     */
    @Override
    public String getServletInfo() {
        return "Short description";
    }// </editor-fold>

}
