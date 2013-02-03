package de.freiburg.uni.tablet.presenter;

import java.util.logging.Level;
import java.util.logging.Logger;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.ImageButton;
import de.freiburg.uni.tablet.presenter.document.DocumentConfig;
import de.freiburg.uni.tablet.presenter.document.ServerDocument;
import de.freiburg.uni.tablet.presenter.editor.rendering.RenderCanvas;
import de.freiburg.uni.tablet.presenter.editor.toolpageeditor.PageEditor;
import de.freiburg.uni.tablet.presenter.page.SolidPen;
import de.freiburg.uni.tablet.presenter.util.SystemUiHider;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 *
 * @see SystemUiHider
 */
public class PageEditorActivity extends Activity {
	private PageEditor _pageEditor;
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        
        Logger.getLogger("de.freiburg.uni.tablet.presenter").setLevel(Level.ALL);

        setContentView(R.layout.activity_page_editor);
        
        final RenderCanvas renderCanvas = (RenderCanvas) findViewById(R.id.render_canvas);
        _pageEditor = new PageEditor(new DocumentConfig("config.ini"), renderCanvas);
        _pageEditor.getDocumentEditor().setDocument(new ServerDocument(1));
        _pageEditor.getDocumentEditor().setCurrentPen(new SolidPen(_pageEditor.getConfig().getFloat("editor.defaultPen.thickness", 1f), _pageEditor.getConfig().getColor("editor.defaultPen.color", 0xff000000)));
        
        final ImageButton next = (ImageButton) findViewById(R.id.next);
        final ImageButton previous = (ImageButton) findViewById(R.id.previous);
        final ImageButton eraser = (ImageButton) findViewById(R.id.tool_eraser);
        final ImageButton scribble = (ImageButton) findViewById(R.id.tool_scribble);
        final ImageButton undo = (ImageButton) findViewById(R.id.undo);
        
        next.setOnClickListener(new ImageButton.OnClickListener() {
			@Override
			public void onClick(final View v) {
				performAction(v.getId(), -1);
			}
		});
        previous.setOnClickListener(new ImageButton.OnClickListener() {
			@Override
			public void onClick(final View v) {
				performAction(v.getId(), -1);
			}
		});
        eraser.setOnClickListener(new ImageButton.OnClickListener() {
			@Override
			public void onClick(final View v) {
				performAction(v.getId(), R.id.tool_primary_group);
			}
		});
        scribble.setOnClickListener(new ImageButton.OnClickListener() {
			@Override
			public void onClick(final View v) {
				performAction(v.getId(), R.id.tool_primary_group);
			}
		});
        undo.setOnClickListener(new ImageButton.OnClickListener() {
			@Override
			public void onClick(final View v) {
				performAction(v.getId(), -1);
			}
		});
    }
    
    @Override
    public boolean onCreateOptionsMenu(final Menu menu) {
    	MenuInflater inflater = getMenuInflater();
    	inflater.inflate(R.menu.main_menu, menu);
    	return super.onCreateOptionsMenu(menu);
    }
    
    private boolean performAction(final int actionId, final int groupActionId) {
    	_pageEditor.processAction(this, actionId, groupActionId);
    	return true;
    }
    
    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
    	super.onOptionsItemSelected(item);
    	return performAction(item.getItemId(), item.getGroupId());
    }
}
