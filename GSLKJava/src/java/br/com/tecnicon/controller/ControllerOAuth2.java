/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.com.tecnicon.controller;

import br.com.tecnicon.police.SingletonGenerico;
import br.com.tecnicon.server.context.TecniconLookup;
import java.io.IOException;
import java.io.PrintWriter;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 *
 * @author eduardo.wohlfahrt
 */
@WebServlet(name = "ControllerOAuth2", urlPatterns =
{
    "/ControllerOAuth2"
})
public class ControllerOAuth2 extends HttpServlet
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
        try
        {
            //TecniconOauth.OAuth2Client.trataRequest
            Object obj = TecniconLookup.lookup("TecniconOauth/OAuth2Client");

            PrintWriter out = response.getWriter();
            out = response.getWriter();
            out.println(obj.getClass().getMethod("trataRequest", HttpServletRequest.class, HttpServletResponse.class).invoke(obj, request, response));
            out.close();
        } catch (Exception ex)
        {
            PrintWriter out = response.getWriter();
            out = response.getWriter();
            out.println("erro:" + ex.getCause().getMessage());
            out.close();
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
            throws ServletException, IOException
    {
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
            throws ServletException, IOException
    {
        String encode = "UTF-8";
        if (!request.getCharacterEncoding().equals(encode))
        {
            request.setCharacterEncoding(encode);
        }
        if (!response.getCharacterEncoding().equals(encode))
        {
            response.setCharacterEncoding(encode);
        }
        // Set standard HTTP/1.1 no-cache headers.
        response.setHeader("Cache-Control", "no-store, no-cache, must-revalidate");
        // Set IE extended HTTP/1.1 no-cache headers (use addHeader).
        response.addHeader("Cache-Control", "post-check=0, pre-check=0");
        // Set standard HTTP/1.0 no-cache header.
        response.setHeader("Pragma", "no-cache");
        response.setDateHeader("Expires", 0);
        processRequest(request, response);
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
