/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.com.tecnicon.controller;

import br.com.tecnicon.criptografia.PWSec;
import br.com.tecnicon.server.context.TecniconLookup;
import java.io.IOException;
import java.io.PrintWriter;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 *
 * @author MAURICIO.SIPMANN
 */
@WebServlet(name = "ControllerAjax", urlPatterns =
{
    "/ControllerAjax"
})
public class ControllerAjax extends HttpServlet
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
        response.addHeader("Access-Control-Allow-Origin", "*");
        response.addHeader("Access-Control-Allow-Methods", "GET, PUT, POST, OPTIONS, DELETE");
        response.addHeader("Access-Control-Allow-Headers", "Content-Type");
        response.addHeader("Access-Control-Max-Age", "86400");
        response.setContentType("text/html;charset=UTF-8");

        try
        {

            try
            {

                String parametros = "";
                String dest = "Ead";
                if (request.getParameter("s") != null)
                {
                    parametros = request.getParameter("s");
                } else if (request.getParameter("u") != null)
                {
                    Object obj = TecniconLookup.lookup("TecniconTrataRequest/Upload");
                    obj.getClass().getMethod("trataUpload", HttpServletRequest.class, HttpServletResponse.class).invoke(obj, request, response);
                    return;
                } else if (request.getParameter("vendasm") != null)
                {
                    dest = "Controller";
                    parametros = request.getParameter("d");
                } else
                {
                    parametros = request.getParameter("d");
                }

                if (!dest.equals("Controller"))
                {
                    parametros = PWSec.decrypt(parametros);
                }
                parametros = parametros.replace("|", "&");

                RequestDispatcher r = request.getRequestDispatcher(dest + "?" + parametros);
                r.forward(request, response);

            } catch (Exception ex)
            {
                PrintWriter out = response.getWriter();
                out.println("erro:" + ex.getMessage());
                out.close();
            }

        } finally
        {

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
