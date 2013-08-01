package com.rytong.ui.editor.lua;

import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.presentation.IPresentationReconciler;
import org.eclipse.jface.text.presentation.PresentationReconciler;
import org.eclipse.jface.text.rules.DefaultDamagerRepairer;
import org.eclipse.jface.text.rules.RuleBasedScanner;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.jface.text.source.SourceViewerConfiguration;

public class LuaSourceViewerConfiguration extends SourceViewerConfiguration {

	private LuaEditor editor;

	public LuaSourceViewerConfiguration(LuaEditor luaEditor) {
    	this.editor = luaEditor;
	}
    
	@Override
	public IPresentationReconciler getPresentationReconciler(
			ISourceViewer sourceViewer) {
      PresentationReconciler reconciler = new PresentationReconciler();
      addDamagerRepairer(reconciler, editor.getLuaScanner(), IDocument.DEFAULT_CONTENT_TYPE);
      return reconciler;
	}

	private void addDamagerRepairer(PresentationReconciler reconciler,
			RuleBasedScanner scanner, String contentType) {
		DefaultDamagerRepairer dr = new DefaultDamagerRepairer(scanner);		
      reconciler.setDamager(dr, contentType);
      reconciler.setRepairer(dr, contentType);
	}

}
