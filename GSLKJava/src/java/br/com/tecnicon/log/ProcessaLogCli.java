package br.com.tecnicon.log;

import br.com.tecnicon.model.MensagemLogCli;
import br.com.tecnicon.server.dataset.TClientDataSet;
import br.com.tecnicon.server.execoes.ExcecaoTecnicon;
import br.com.tecnicon.server.sessao.VariavelSessao;

public class ProcessaLogCli implements Runnable
{
    
    MensagemLogCli msgCli;

    public ProcessaLogCli(MensagemLogCli msgCli)
    {
        this.msgCli = msgCli;
    }
    
    public void run()
    {
        
        /*if (msgCli != null)
        {
            try
            {
                VariavelSessao vs = new TVariavelSessao();
                vs.addParametros("empresa", "17");
                vs.addParametros("senha", "");
                vs.addParametros("filial", "1");
                vs.addParametros("sessao", "-9878");
                vs.addParametros("local", "1");
                vs.addParametros("cusuario", "");
                vs.addParametros("nome", "");
                vs.addParametros("parametro", "parsql=");
                vs.addParametros("usuario","");
               
                TClientDataSet CDSTEMP = obterClientDataSet(vs, msgCli.getTipoLog());
                CDSTEMP.XMLData(msgCli.getXMLData());
                CDSTEMP.open();
                CDSTEMP.first();
                
                if ( msgCli.getTipoLog().equals("ERRO") )
                {
                    TClientDataSet CDSERRO = TClientDataSet.create(vs, "LOGERROCLI");
                    CDSERRO.createDataSet();
                    while(!CDSTEMP.eof())
                    {
                        CDSERRO.insert();
                        CDSERRO.fieldByName("DATA").asDate(CDSTEMP.fieldByName("DATA").asDate());
                        CDSERRO.fieldByName("HORA").asTime(CDSTEMP.fieldByName("HORA").asDate());
                        CDSERRO.fieldByName("USUARIO").asString(CDSTEMP.fieldByName("USUARIO").asString());
                        CDSERRO.fieldByName("TELA").asString(CDSTEMP.fieldByName("TELA").asString());
                        CDSERRO.fieldByName("MENSAGEM").asString(CDSTEMP.fieldByName("MENSAGEM").asString());
                        CDSERRO.fieldByName("VERSAO").asInteger(CDSTEMP.fieldByName("VERSAO").asInteger());
                        CDSERRO.fieldByName("ERRO").asString(CDSTEMP.fieldByName("ERRO").asString());
                        CDSERRO.fieldByName("CODCLISITE").asString(msgCli.getCodCliSite());
                        CDSERRO.post();
                        CDSTEMP.next();
                    }
                    
                }
                else if ( msgCli.getTipoLog().equals("REL") )
                {
                    TClientDataSet CDSREL= TClientDataSet.create(vs, "LOGRELCLI");
                    CDSREL.createDataSet();
                    while(!CDSTEMP.eof())
                    {
                        CDSREL.insert();
                        CDSREL.fieldByName("DATA").asDate(CDSTEMP.fieldByName("DATA").asDate());
                        CDSREL.fieldByName("HORAI").asTime(CDSTEMP.fieldByName("HORAI").asDate());
                        CDSREL.fieldByName("HORAF").asString(CDSTEMP.fieldByName("HORAF").isNull()?null:CDSTEMP.fieldByName("HORAF").asString());
                        CDSREL.fieldByName("NUMREL").asInteger(CDSTEMP.fieldByName("NUMREL").asInteger());
                        CDSREL.fieldByName("PARAMETRO").asString(CDSTEMP.fieldByName("PARAMETRO").asString());
                        CDSREL.fieldByName("USUARIO").asString(CDSTEMP.fieldByName("USUARIO").asString());
                        CDSREL.fieldByName("CODCLISITE").asString(msgCli.getCodCliSite());
                        CDSREL.post();
                        CDSTEMP.next();
                    }
                    
                }
                else if ( msgCli.getTipoLog().equals("RELUSO") )
                {
                    TClientDataSet CDSRELUSO= TClientDataSet.create(vs, "LOGRELUSO");
                    CDSRELUSO.createDataSet();
                    while(!CDSTEMP.eof())
                    {
                        CDSRELUSO.insert();
                        CDSRELUSO.fieldByName("DATA").asDate(CDSTEMP.fieldByName("DATA").asDate());
                        CDSRELUSO.fieldByName("NUMREL").asInteger(CDSTEMP.fieldByName("NUMREL").asInteger());
                        CDSRELUSO.fieldByName("QTDE").asString(CDSTEMP.fieldByName("QTDE").asString());
                        CDSRELUSO.fieldByName("CODCLISITE").asString(msgCli.getCodCliSite());
                        CDSRELUSO.post();
                        CDSTEMP.next();
                    }
                    
                }
            } catch (ExcecaoTecnicon ex)
            {
            }
        }*/
        
    }
    
    private TClientDataSet obterClientDataSet(VariavelSessao vs,String tipo) throws ExcecaoTecnicon
    {
        TClientDataSet CDSTEMP = TClientDataSet.create(vs);
        if (tipo.equals("ERRO"))
        {
             
            CDSTEMP.fieldDefs().clear();
            CDSTEMP.fieldDefs().add("DATA", "ftDate", 0, false);
            CDSTEMP.fieldDefs().add("HORA", "ftTime", 0, false);
            CDSTEMP.fieldDefs().add("USUARIO", "ftString", 31, false);
            CDSTEMP.fieldDefs().add("TELA", "ftString", 100, false);
            CDSTEMP.fieldDefs().add("MENSAGEM", "ftString", 200, false);
            CDSTEMP.fieldDefs().add("VERSAO", "ftInteger", 0, false);
            CDSTEMP.fieldDefs().add("ERRO", "ftMemo", 80, false);
            CDSTEMP.createDataSet();
        }
        else if(tipo.equals("REL"))
        {
            
            CDSTEMP.fieldDefs().clear();
            CDSTEMP.fieldDefs().add("DATA", "ftDate", 0, false);
            CDSTEMP.fieldDefs().add("HORAI", "ftTime", 0, false);
            CDSTEMP.fieldDefs().add("HORAF", "ftTime", 0, false);
            CDSTEMP.fieldDefs().add("NUMREL", "ftInteger", 0, false);
            CDSTEMP.fieldDefs().add("PARAMETRO", "ftMemo", 80, false);
            CDSTEMP.fieldDefs().add("USUARIO", "ftString", 31, false);
            CDSTEMP.createDataSet();
        }
        else if(tipo.equals("RELUSO"))
        {
            
            CDSTEMP.fieldDefs().clear();
            CDSTEMP.fieldDefs().add("DATA", "ftDate", 0, false);
            CDSTEMP.fieldDefs().add("NUMREL", "ftInteger", 0, false);
            CDSTEMP.fieldDefs().add("QTDE", "ftInteger", 0, false);
            CDSTEMP.createDataSet();
        }
        return CDSTEMP;
            
    }
            
    
}
