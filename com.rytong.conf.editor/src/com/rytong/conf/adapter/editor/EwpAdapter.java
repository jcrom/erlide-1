package com.rytong.conf.adapter.editor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.erlide.jinterface.ErlLogger;

import com.ericsson.otp.erlang.OtpErlangAtom;
import com.ericsson.otp.erlang.OtpErlangInt;
import com.ericsson.otp.erlang.OtpErlangList;
import com.ericsson.otp.erlang.OtpErlangObject;
import com.ericsson.otp.erlang.OtpErlangTuple;
import com.rytong.conf.util.ConfParams;

public class EwpAdapter {


    private String name;
    private String host;
    private String protocol;
    private String port;
    private String return_type;
    private HashMap<String, EwpProcedure> childrenMap;

    private String oldName;

    private boolean status = true;
    private String errMsg = "";


    private static String sname = "name";
    private static String shost = "host";
    private static String sprotocol = "protocol";
    private static String sport = "port";
    private static String sreturn_type = "return_type";

    public EwpAdapter(){
        name = "";
        host = "";
        protocol = "http";
        port = "";
        return_type = "xml";
        childrenMap = new HashMap<String, EwpProcedure>();
        oldName = "";

    }

    public boolean has_children(){
        if (childrenMap.size() >0){
            return true;
        } else
            return false;
    }

    /**
     * 为adapter复制
     *
     */
    public void setValue(String id, String value){
        if(id == sname){
            this.name = value;
            this.oldName = value;
        }else if(id == sreturn_type)
            return_type = value;
        else if(id == shost)
            host = value;
        else if(id == sprotocol)
            protocol = value;
        else if(id == sport)
            port = value;
        else{
            ErlLogger.debug("error params!");
        }
    }

    /**
     * 判断adapter的必须项是否为空
     */
    public boolean checkNeededValue(){
        if(name.replace(" ", "").isEmpty()||host.replace(" ", "").isEmpty())
            return false;
        else
            return true;
    }
    /**
     * 赋值
     */
    public void setStatus(){
        status = true;
    }

    public void setErrStatus(){
        status = false;
    }

    public boolean getStatus(){
        return status;
    }

    public void setErrMsg(String msg){
        errMsg = msg;
    }

    public void setName(String name){
        this.name = name;
    }

    public void setHost(String host){
        this.host = host;
    }

    public void setProtocol(String protocol){
        this.protocol = protocol;
    }

    public void setPort(String port){
        this.port = port;
    }

    public void setReturnType(String return_type){
        this.return_type = return_type;
    }

    /**
     * 为adapter添加子procedure
     * @param tmpProcedure
     */
    public void setChildren(EwpProcedure tmpProcedure){
        String adapterName = tmpProcedure.getAdapter();
        if (adapterName.equalsIgnoreCase(name)){
            childrenMap.put(tmpProcedure.getId(), tmpProcedure);
        }
    }
    /**
     * 删除该adapter的某个子procedure
     * @param id
     */
    public void removeChildren(String id){
        childrenMap.remove(id);
    }

    /**
     * 刷新adapter的指定procedure
     * @param tmpProcedure
     */
    public void refreshChildren(EwpProcedure tmpProcedure){
        childrenMap.remove(tmpProcedure.getId());
        childrenMap.put(tmpProcedure.getId(), tmpProcedure);
    }

    public void setOldName(String oldName){
        this.oldName = oldName;
    }

    public String getName(){
        return name;
    }

    public String getHost(){
        return host;
    }

    public String getProtocol(){
        return protocol;
    }

    public String getPort(){
        return port;
    }

    public String getReturnType(){
        return return_type;
    }

    public EwpProcedure getChildren(String Id){
        return childrenMap.get(Id);
    }

    public Object[] getChildrenArray (){
        return childrenMap.values().toArray();
    }

    /**
     * 获取该adapter的子procedure列表
     * @return EwpProcedure List
     */
    public List<EwpProcedure> getChildrenList (){
        ArrayList<EwpProcedure> list = new ArrayList<EwpProcedure>();
        for(EwpProcedure obj: childrenMap.values()){
            list.add(obj);
        }
        return list;
    }

    /**
     * 拼接adapter为 erlang tuple，用于和erlang之间的交互
     * @return OtpErlangTuple
     */
    public OtpErlangTuple form_remove_index(){
        //Name, Host, Port, Protocol, ReturnType
        OtpErlangObject[] request = new OtpErlangObject[2];
        request[0] = new OtpErlangList(name);
        OtpErlangList props = get_child_list();
        if (props!=null)
            request[1]=props;
        else
            request[1]=new OtpErlangList();
        return new OtpErlangTuple(request);
    }

    private OtpErlangList get_child_list(){
        ArrayList<String> list = new ArrayList<String>();
        Map<String, EwpProcedure> map = childrenMap;
        Iterator<Entry<String, EwpProcedure>> adpiter = map.entrySet().iterator();
        while (adpiter.hasNext()) {
            Map.Entry entry = (Map.Entry) adpiter.next();
            String tmpKey = (String) entry.getKey();
            //ErlLogger.debug("cha key:"+key);
            list.add(tmpKey);
        }
        if (list.size()!=0){
            OtpErlangObject[] result = new OtpErlangObject[list.size()];
            for(int i=0; i<list.size();i++)
                result[i]=new OtpErlangList(list.get(i));
            return new OtpErlangList(result);
        }
        else
            return null;
    }

    /**
     * 拼接adapter为 erlang tuple，用于和erlang之间的交互
     * @return OtpErlangTuple
     */
    public OtpErlangTuple formAdapter(){
        //Name, Host, Port, Protocol, ReturnType
        OtpErlangObject[] request = new OtpErlangObject[5];
        request[0] = new OtpErlangList(name);
        request[1] = new OtpErlangList(host);
        request[2] = new OtpErlangList(port);
        request[3] = new OtpErlangAtom(protocol);
        request[4] = new OtpErlangAtom(return_type);
        return new OtpErlangTuple(request);
    }


    public OtpErlangTuple editAdapterName(){
        return editAdapterTuple(sname, name);
    }

    public OtpErlangTuple editAdapterHost(){
        return editAdapterTuple(shost, host);
    }

    public OtpErlangTuple editAdapterPort(){
        return editAdapterTupleIntValue(sport, port);
    }

    public OtpErlangTuple editAdapterProtocol(){
        return editAdapterTupleAtomValue(sprotocol, protocol);
    }

    public OtpErlangTuple editAdapterReturnType(){
        return editAdapterTupleAtomValue(sreturn_type, return_type);
    }

    /**
     * 拼接adapter为 erlang tuple，用于和erlang之间的交互，
     * 主要用于adapter的某个值修改
     * @return OtpErlangTuple
     * {name:List, key:atom, Value:List}
     */
    private OtpErlangTuple editAdapterTuple(String Key, String Value){
        // {OldName, Name, RVal}
        //Id, App, Name, Entry, Views, Props, State
        OtpErlangObject[] request = new OtpErlangObject[3];
        request[0] = new OtpErlangList(oldName);
        request[1] = new OtpErlangAtom(Key);
        request[2] = new OtpErlangList(Value);
        return new OtpErlangTuple(request);
    }

    /**
     * 拼接adapter为 erlang tuple，用于和erlang之间的交互，
     * 主要用于adapter的某个值修改
     * @return OtpErlangTuple
     * {name:List, key:atom, atom}
     */
    private OtpErlangTuple editAdapterTupleAtomValue(String Key, String Value){
        // {OldName, Name, RVal}
        //Id, App, Name, Entry, Views, Props, State
        OtpErlangObject[] request = new OtpErlangObject[3];
        request[0] = new OtpErlangList(oldName);
        request[1] = new OtpErlangAtom(Key);
        request[2] = new OtpErlangAtom(Value);
        return new OtpErlangTuple(request);
    }

    /**
     * 拼接adapter为 erlang tuple，用于和erlang之间的交互，
     * 主要用于adapter的某个值修改
     * @return OtpErlangTuple
     * {name:List, key:atom, Value:Int}
     */
    private OtpErlangTuple editAdapterTupleIntValue(String Key, String Value){
        // {OldName, Name, RVal}
        //Id, App, Name, Entry, Views, Props, State
        OtpErlangObject[] request = new OtpErlangObject[3];
        request[0] = new OtpErlangList(oldName);
        request[1] = new OtpErlangAtom(Key);
        request[2] = new OtpErlangInt(Integer.valueOf(Value));
        return new OtpErlangTuple(request);
    }

}
