package com.rytong.conf.newchannel.wizard;


import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;
import org.erlide.jinterface.ErlLogger;

import com.rytong.conf.editor.pages.EwpChannels;
import com.rytong.conf.newchannel.wizard.NewChaWizardDetailPage.AddDiaolog;
import com.rytong.conf.util.ChannelConfUtil;
import com.rytong.conf.util.ChannelConfUtil.viewDiaolog;

public class OldCallBackChannel {
    private NewChaWizard wizard;
    private NewChaWizardViewPage parent;
    private ChannelConfUtil confUtil;
    private EwpChannels cha;
    private String selectId;


    private Label csLabel;
    private Button csBut;
    private boolean csFlag;

    protected Composite composite;
    private Table table;
    private Button addBut;
    private Button editBut;
    private Button removeBut;
    private Button remAllBut;

    private boolean addFlagGlobal = true;

    public OldCallBackChannel(NewChaWizard wizard, NewChaWizardViewPage parent){
        this.wizard = wizard;
        this.parent = parent;
        cha = wizard.cha;
        this.selectId = wizard.selectId;
        confUtil = parent.confUtil;
    }

    public Composite initial_composite(){

        composite = new Group(parent.parentcomposite, SWT.BORDER);

        FormData template_form = new FormData();
        template_form.left = new FormAttachment(0);
        template_form.right = new FormAttachment(100,-5);
        template_form.top = new FormAttachment(0,30);
        template_form.bottom = new FormAttachment(100, -3);
        composite.setLayoutData(template_form);
        composite.setLayout(new FormLayout());
        //templateGroup.setText("Template List");

        csLabel = new Label(composite, SWT.NONE);
        csLabel.setText("生成CS模板");
        FormData csl_form = new FormData();
        csl_form.left = new FormAttachment(0,40);
        csl_form.right = new FormAttachment(100, -10);
        csl_form.top = new FormAttachment(0, 10);
        csLabel.setLayoutData(csl_form);
        csBut = new Button(composite, SWT.BORDER | SWT.CHECK);
        FormData csb_form = new FormData();
        csb_form.left = new FormAttachment(0,20);
        csb_form.right = new FormAttachment(0, 40);
        csb_form.top = new FormAttachment(0,10);
        csBut.setLayoutData(csb_form);
        csBut.addListener(SWT.Selection, setCheckBoxListener());

        draw_table(composite);
        initial_element();

        ErlLogger.debug("entry:"+cha.cha_entry);

        return composite;
    }

    public void draw_table(Composite composite){
        table = new Table(composite, SWT.BORDER | SWT.MULTI);
        TableColumn tranColumn = new TableColumn(table, SWT.NONE);
        tranColumn.setText("tranCode");
        tranColumn.setWidth(200);
        TableColumn viewColumn = new TableColumn(table, SWT.NONE);
        viewColumn.setText("viewName");
        viewColumn.setWidth(200);

        table.setHeaderVisible(true);
        table.setLinesVisible(true);

        FormData table_form = new FormData();
        table_form.top = new FormAttachment(0, 40);
        table_form.left = new FormAttachment(0, 20);
        table_form.right = new FormAttachment(100, -170);
        table_form.bottom = new FormAttachment(100, -20);
        table.setLayoutData(table_form);

        addBut = new Button(composite, SWT.BORDER | SWT.CENTER);
        addBut.setText("Add...");
        addBut.setLayoutData(setButtonLayout(0));

        editBut = new Button(composite, SWT.BORDER | SWT.CENTER);
        editBut.setText("Edit");
        editBut.setLayoutData(setButtonLayout(1));

        removeBut = new Button(composite, SWT.BORDER | SWT.CENTER);
        removeBut.setText("Remove");
        removeBut.setLayoutData(setButtonLayout(2));

        remAllBut = new Button(composite, SWT.BORDER | SWT.CENTER);
        remAllBut.setText("Remove All");
        remAllBut.setLayoutData(setButtonLayout(3));

        setTableListener();
        setButtonListener();

    }

    public void initial_element(){
        table.setEnabled(false);
        addBut.setEnabled(false);
        editBut.setEnabled(false);
        removeBut.setEnabled(false);
        remAllBut.setEnabled(false);
    }

    public void initial_text(){
        table.setEnabled(true);
        HashMap<TableItem, OldCallbackParams> tmpOldViewMap = cha.add_view.oldViewMap;
        Map<TableItem, OldCallbackParams> map = tmpOldViewMap;
        Iterator<Entry<TableItem, OldCallbackParams>> chaiter = map.entrySet().iterator();
        while(chaiter.hasNext()){
            Entry<TableItem, OldCallbackParams> tmpIter = chaiter.next();
            OldCallbackParams viewParam = tmpIter.getValue();
            addParamsItem(table, viewParam);
        }
        editBut.setEnabled(false);
        removeBut.setEnabled(false);
        if (table.getItemCount() == 0)
            remAllBut.setEnabled(false);
    }


    public TableItem addParamsItem(Table table, OldCallbackParams tmpParams){
        TableItem tmpItem = new TableItem(table, SWT.NONE);
        tmpItem.setText(new String[] {tmpParams.tranCode, tmpParams.viewName});
        return tmpItem;
    }


    private static TableItem[] table_item;
    public void setTableListener(){
        table.addMouseListener(new MouseAdapter(){
             public void mouseDown(MouseEvent event) {
                    ErlLogger.debug("table event.");
                    if (event.getSource() != null){
                        table_item = table.getSelection();
                        if (table_item.length == 1){
                            ErlLogger.debug("table event:"+table_item.length);
                            editBut.setEnabled(true);
                            removeBut.setEnabled(true);
                        } else if (table_item.length > 1){
                            ErlLogger.debug("table event:"+table_item.length);
                            editBut.setEnabled(false);
                            removeBut.setEnabled(true);
                            remAllBut.setEnabled(true);
                        } else {
                            editBut.setEnabled(false);
                            removeBut.setEnabled(false);
                        }
                    } else {
                        ErlLogger.debug("table event: null");
                    }
             }
        });
    }

    public void setButtonListener(){
        addBut.addSelectionListener(new SelectionAdapter(){
            public void widgetSelected(SelectionEvent e) {
                ErlLogger.debug("add button!");
                OldCallbackParams tmpOld = new OldCallbackParams();
                viewDiaolog newDialog = confUtil.newViewDiaolog(parent.getShell(), addFlagGlobal, tmpOld, cha.cha_id);
                newDialog.open();
                ErlLogger.debug("dialog result :"+newDialog.getReturnCode());
                if (newDialog.getReturnCode()==Window.OK){
                    TableItem tmpItem = new TableItem(table, SWT.BORDER);
                    tmpOld.viewName = tmpOld.viewName.replace(" ", "");
                    tmpItem.setText(new String[]{tmpOld.tranCode, tmpOld.viewName});
                    cha.add_view.addOldView(tmpItem, tmpOld);
                    ErlLogger.debug("old list:"+cha.add_view.oldViewMap.size());
                    remAllBut.setEnabled(true);
                }
            }
        });

        editBut.addSelectionListener(new SelectionAdapter(){
            public void widgetSelected(SelectionEvent e) {
                ErlLogger.debug("edit button!");
                OldCallbackParams tmpOld = cha.add_view.getOldView(table_item[0]);
                ErlLogger.debug("edit button:"+tmpOld.tranCode);
                viewDiaolog newDialog = confUtil.newViewDiaolog(parent.getShell(), !addFlagGlobal, tmpOld, cha.cha_id);
                newDialog.open();
                ErlLogger.debug("dialog result :"+newDialog.getReturnCode());
                if (newDialog.getReturnCode()==Window.OK){
                    tmpOld.viewName = tmpOld.viewName.replace(" ", "");
                    table_item[0].setText(new String[]{tmpOld.tranCode, tmpOld.viewName});
                    cha.add_view.refreshOldView(table_item[0], tmpOld);
                    ErlLogger.debug("old list:"+cha.add_view.oldViewMap.size());
                }

            }
        });

        removeBut.addSelectionListener(new SelectionAdapter(){
            public void widgetSelected(SelectionEvent e) {
                ErlLogger.debug("remove button:"+table_item[0]);
                if (table.getItemCount() != 0){
                    table_item[0].dispose();
                    //table.remove(table_item[0]);
                    cha.add_view.removeOldView(table_item[0]);
                    remAllBut.setEnabled(false);
                    removeBut.setEnabled(false);
                }
            }
        });

        remAllBut.addSelectionListener(new SelectionAdapter(){
            public void widgetSelected(SelectionEvent e) {
                ErlLogger.debug("remove all button!");
                table.removeAll();
                cha.add_view.clearOldView();
                removeBut.setEnabled(false);
                remAllBut.setEnabled(false);
            }
        });
    }


    public Listener setCheckBoxListener(){
        Listener tmpListener = new Listener(){
            @Override
            public void handleEvent(Event event) {
                Button tmpBut = (Button) event.widget;
                 if (tmpBut == csBut) {
                    ErlLogger.debug("csBut bug!");
                    csFlag = ChannelConfUtil.getFlag(tmpBut.getSelection());
                    cha.getViewMap().setOCBCsFlag(csFlag);
                    ErlLogger.debug("csBut bug:"+csFlag);
                    setTableSt();
                    //testCs();
                } else {
                    ErlLogger.debug("else bug!");
                }
            }
        };
        return tmpListener;
    }

    public void setTableSt(){
        if (csFlag ){
            table.setEnabled(true);
            addBut.setEnabled(true);
            if (table.getItemCount()>0)
                remAllBut.setEnabled(true);
        } else {
            table.setEnabled(false);
            addBut.setEnabled(false);
            editBut.setEnabled(false);
            removeBut.setEnabled(false);
            remAllBut.setEnabled(false);
        }
    }

    public void set_visiable(){
        composite.setVisible(true);
    }

    public void set_unvisiable(){
        composite.setVisible(false);
    }

    /***
     * form data
     */
    private FormData setButtonLayout(int i){
        FormData comsite_form = new FormData();
        comsite_form.left = new FormAttachment(100, -150);
        comsite_form.right = new FormAttachment(100, -20);
        comsite_form.top = new FormAttachment(0, 45+i*35);
        return comsite_form;
    }

}
