/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package br.com.tecnicon.controller;

import br.com.tecnicon.server.dataset.TClientDataSet;
import br.com.tecnicon.server.execoes.ExcecaoTecnicon;
import br.com.tecnicon.server.sessao.VariavelSessao;
import java.util.Date;
import javax.ejb.Stateless;

/**
 *
 * @author mauricio.sipmann
 */
@Stateless
public class GravaTabErro
{

    public String grava(VariavelSessao vs)
    {
        try
        {
            TClientDataSet cds = TClientDataSet.create(vs, "TABERRO");
            cds.createDataSet();
            cds.open();

            cds.insert();
            cds.fieldByName("DATA").asDate(new Date());
            cds.fieldByName("HORA").asTime(new Date());
            cds.fieldByName("USUARIO").asString(vs.getValor("usuario"));
            cds.fieldByName("TELA").asString("JS: " + vs.getParameter("url"));
            cds.fieldByName("MENSAGEM").asString(vs.getParameter("mensagem"));
            cds.fieldByName("VERSAO").asString(vs.getValor("versao").replace(".", ""));
            cds.fieldByName("ERRO").asString(vs.getValor("linha"));
            cds.post();
            return "ok";
        } catch (ExcecaoTecnicon ex) {
            return "erro";
        }
    }

}
