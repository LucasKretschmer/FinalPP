package br.com.tecnicon.controller;

import br.com.tecnicon.paginas.Index;
import br.com.tecnicon.police.ListaSessoes;
import br.com.tecnicon.police.ListaUsuarios;
import br.com.tecnicon.server.Propriedades;
import br.com.tecnicon.server.execoes.ExcecaoTecnicon;
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
 * @author jean.siqueira
 */
@WebServlet(name = "Colaborador", urlPatterns =
{
    "/Colaborador"
})
public class Colaborador extends HttpServlet
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
        //processRequest(request, response);
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

        request.setCharacterEncoding("UTF-8");
        response.setContentType("text/html");
        response.setCharacterEncoding("UTF-8");

        if (request.getSession().getAttribute("vs") != null)
        {
            request.getSession().setAttribute("vs", null);
        }

        if (request.getParameter("dispositivo") == null)
        {
            response.sendRedirect("/Tecnicon/indexColaborador.html");
        }
        else
        {
            String ipAddress = request.getHeader("x-forwarded-for");
            if (ipAddress == null)
            {
                ipAddress = request.getHeader("X_FORWARDED_FOR");
                if (ipAddress == null)
                {
                    ipAddress = request.getRemoteAddr();
                }
            }

            // Set standard HTTP/1.1 no-cache headers.
            response.setHeader("Cache-Control", "no-store, no-cache, must-revalidate");

            // Set IE extended HTTP/1.1 no-cache headers (use addHeader).
            response.addHeader("Cache-Control", "post-check=0, pre-check=0");

            // Set standard HTTP/1.0 no-cache header.
            response.setHeader("Pragma", "no-cache");

            response.setDateHeader("Expires", 0);

            request.getSession(true);

            String width = request.getParameter("width");
            String height = request.getParameter("height");
            String navegador = request.getParameter("navegador");
            String dispositivo = request.getParameter("dispositivo");
            String versao = request.getParameter("versao");
            String so = request.getParameter("so");
            String navegadordelphi = request.getParameter("navegadordelphi");
            String navegadorswing = request.getParameter("navegadorswing");
            String parametros = request.getParameter("parametros");
            String tipoacesso = request.getParameter("tipoacesso");

            if (navegadordelphi.equalsIgnoreCase("TRUE"))
            {
                request.getSession(true).setMaxInactiveInterval(-1);
            }
            else
            {
                request.getSession(true).setMaxInactiveInterval((Propriedades.obterTempoSessao(request.getRemoteAddr())) * 60);
            }
            
            if (navegadordelphi == null)
            {
                navegadordelphi = "false";
            }
            else if (!navegadordelphi.equals("true"))
            {
                navegadordelphi = "false";
            }

            if (navegadorswing == null)
            {
                navegadorswing = "false";
            }
            else if (!navegadorswing.equals("true"))
            {
                navegadorswing = "false";
            }
            //request.;
            request.getSession(true).setAttribute("width", width);
            request.getSession(true).setAttribute("height", height);
            request.getSession(true).setAttribute("navegador", navegador);
            request.getSession(true).setAttribute("dispositivo", dispositivo);
            request.getSession(true).setAttribute("versao", versao);
            request.getSession(true).setAttribute("so", so);

            PrintWriter out = response.getWriter();
            out = response.getWriter();

            if (parametros != null && !parametros.equals(""))
            {

                String[] arrParametros = parametros.split("\\|");
                String campo;
                String valor;
                String[] arrParametro;
                String sessao = null;
                String empresa = null;
                String filial = null;
                String local = null;
                String cusuario = null;
                String nEmpresa = null;
                String nFilial = null;
                String nLocal = null;
                String versaobanco = null;
                String versaosis = null;
                for (String param : arrParametros)
                {
                    arrParametro = param.split("=");
                    campo = arrParametro[0];
                    valor = arrParametro[1];

                    if (campo.equals("cusuario"))
                    {
                        cusuario = valor;
                    }
                    else if (campo.equals("sessao"))
                    {
                        sessao = valor; //VALIDA NA CONTROLLER
//                        ArrayList<String> a = JWTCheck.check(sessao, 
//                                request.getHeader(JWTTec.TOKEN_HEADER));
//                        sessao = a.get(0);
                    }
                    else if (campo.equals("empresa"))
                    {
                        empresa = valor;
                    }
                    else if (campo.equals("filial"))
                    {
                        filial = valor;
                    }
                    else if (campo.equals("local"))
                    {
                        local = valor;
                    }
                    else if (campo.equals("nempresa"))
                    {
                        nEmpresa = valor;
                    }
                    else if (campo.equals("nfilial"))
                    {
                        nFilial = valor;
                    }
                    else if (campo.equals("nlocal"))
                    {
                        nLocal = valor;
                    }
                    else if (campo.equals("versaosis"))
                    {
                        versaosis = valor;
                    }
                    else if (campo.equals("versaobanco"))
                    {
                        versaobanco = valor;
                    }

                }

                ListaUsuarios listaUs = ListaUsuarios.getInstance();
                ListaSessoes listaSessoes = ListaSessoes.getInstance();
                VariavelSessao vs = new TVariavelSessao();
                vs.addParametros("empresa", empresa);
                vs.addParametros("cusuario", cusuario);

                String nome = "";
                try
                {
                    nome = listaUs.getNomeByCod(vs, "" + cusuario);
                } catch (ExcecaoTecnicon ex)
                {
                }
                
                vs.addParametros("nome", nome);
                vs.addParametros("usuario", nome);
                vs.addParametros("sessao", sessao);

                try
                {
                    vs.addParametros("email", listaUs.getEmailByCod(vs, cusuario));
                } catch (ExcecaoTecnicon ex)
                {
                    vs.addParametros("email", "");
                }
                
                try
                {
                    vs.addParametros("senha", listaUs.getSenhaByCodigo(vs, cusuario));
                } catch (ExcecaoTecnicon ex)
                {
                    vs.addParametros("senha", "");
                }

                vs.addParametros("filial", filial);
                vs.addParametros("local", local);
                vs.addParametros("nempresa", nEmpresa);
                vs.addParametros("nfilial", nFilial);
                vs.addParametros("nlocal", nLocal);
                vs.addParametros("versaobanco", versaobanco);
                vs.addParametros("versaosis", versaosis);
                vs.addParametros("ipcliente", request.getRemoteAddr());
                vs.addParametros("sessionid", request.getSession().getId());
                
                try
                {
                    sessao = listaSessoes.criarSessaoURL(vs);
                } catch (Exception e)
                {
                    out = response.getWriter();
                    out.println("erro:Não foi possível criar uma sessão");
                    
                    new ExcecaoTecnicon(vs, "Não foi possível criar uma sessão", e, true);
                    
                    return;
                } finally
                {
                    if (out != null)
                    {
                        try
                        {
                            out.flush();
                        } catch (Exception e)
                        {
                        }
                        try
                        {
                            out.close();
                        } catch (Exception e)
                        {
                        }
                    }
                }
                
                request.getSession().setAttribute("vs", vs);

                out.println(Index.retornaPrincipal(dispositivo, navegadordelphi, vs, navegadorswing, request));
                out.close();

            }
            else
            {
                try
                {
                    out.println(Index.retornaIndex(dispositivo, navegadordelphi, tipoacesso, navegadorswing, "colaborador", request));
                    out.close();
                }
                catch (ExcecaoTecnicon ex)
                {
                    out = response.getWriter();
                    out.println("erro:" + ex.getMessage());
                    out.close();
                }

            }
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
