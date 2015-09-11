package com.gotako.govoz;

import static com.gotako.govoz.VozConstant.*;
import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.DialogFragment;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.FrameLayout;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import com.bugsense.trace.BugSenseHandler;
import com.bugsense.trace.ExceptionCallback;
import com.gotako.gofast.GoFastEngine;
import com.gotako.gofast.ReactiveCollectionField;
import com.gotako.gofast.annotation.BindingCollection;
import com.gotako.govoz.data.Forum;
import com.gotako.govoz.data.NavDrawerItem;
import com.gotako.govoz.data.Thread;
import com.gotako.govoz.tasks.VozForumDownloadTask;

import info.hoang8f.android.segment.SegmentedGroup;

public class ForumActivity extends VozFragmentActivity implements
		ActivityCallback<Thread>, OnItemClickListener, ExceptionCallback {
	private List<Forum> forums;
	ListView list;
	@BindingCollection(id = R.id.threadsList, layout = R.layout.thread_item, twoWay = true)
	private List<Thread> threads;
	private int lastPage = 1;
	private ListView listView;

	@SuppressWarnings("rawtypes")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		VozCache.instance().setCanShowReplyMenu(false);


        LayoutInflater mInflater = (LayoutInflater) getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
		View layout = mInflater.inflate(R.layout.activity_forum, null);

		FrameLayout frameLayout = (FrameLayout) findViewById(R.id.frame_container);
		frameLayout.addView(layout);
		/*
		 * overridePendingTransition(R.animator.right_slide_in,
		 * R.animator.left_slide_out);
		 */
		overridePendingTransition(R.animator.right_slide_in,
				R.animator.zoom_out);
		forums = new ArrayList<Forum>();
		threads = new ArrayList<Thread>();
		GoFastEngine.initialize(this);
		ReactiveCollectionField field = GoFastEngine.instance()
				.getBindingObject(this, "threads",
						ReactiveCollectionField.class);
		field.getAdapter().setBindingActionListener(this);
		final FragmentActivity activity = this;
		list = (ListView) layout.findViewById(R.id.threadsList);
		list.setOnItemClickListener(this);
        processNavigationLink();
        updateNavigationPanel();
	}

    private void processNavigationLink() {
        // last element of list should be forum link
        String forumLink = VozCache.instance().navigationList.get(VozCache.instance().navigationList.size() - 1);
        String[] parameters = forumLink.split("\\?")[1].split("\\&");
        String firstParam = parameters[0];
        String forumId = firstParam.split("=")[1];
        String currentForumId = VozCache.instance().getCurrentForum().getId();
        if(currentForumId.equals(forumId)) {
            if (VozCache.instance().cache().containsKey(VozConstant.FORUM_THREADS + currentForumId)) {
                reloadThreadsFromCache();
            } else { // load threads
                loadThreads();
            }
        } else {
            int foundIndex = -1;
            for(int i =1; i < parameters.length; i ++) {
                if(parameters[i].startsWith("page=")) {
                    foundIndex =i;
                    break;
                }
            }
            int forumPage = 1;
            if(foundIndex > -1) forumPage = Integer.parseInt(parameters[foundIndex].split("\\=")[1]);
            loadThreads(forumId, forumPage);
        }
    }

    @SuppressWarnings("unchecked")
	private void reloadThreadsFromCache() {
		threads = (List<Thread>) VozCache.instance().cache().get(VozConstant.FORUM_THREADS + VozCache.instance().getCurrentForum().getId());
		GoFastEngine.notify(this, "threads");
		lastPage = (Integer) VozCache.instance().cache().get(VozConstant.FORUM_LAST_PAGE + VozCache.instance().getCurrentForum().getId());
		updateStatus();
		if (VozCache.instance().cache().containsKey(FORUM_POSITION)) {
			int pos = (Integer) VozCache.instance().cache().get(FORUM_POSITION);
			if (pos < threads.size())
				listView.setSelection(pos);
		}
		forums = (List<Forum>) VozCache.instance().cache().get(SUB_FORUMS + VozCache.instance().getCurrentForum().getId());
	}

	private void updateStatus() {
		Forum forum = VozCache.instance().getCurrentForum();
		setTitle(forum.getForumName());
		listView = (ListView) findViewById(R.id.threadsList);
		// listView.smoothScrollToPosition(0);
		listView.setSelection(0);
        updateNavigationPanel();
	}

    public void loadThreads() {
        loadThreads(VozCache.instance().getCurrentForum().getId(), VozCache.instance().getCurrentForumPage());
    }
	public void loadThreads(String forumId, int page) {
		VozForumDownloadTask task = new VozForumDownloadTask(this);
        String _forumId = forumId;
        if(forumId == null) {
            _forumId = VozCache.instance().getCurrentForum().getId();
        }
        int _page = page;
        if(_page == 0) {
            _page = VozCache.instance().getCurrentForumPage();
        }
		// load threads for forum
		task.setShowProcessDialog(true);
		task.setContext(this);
		task.setRetries(1);
		String forumUrl = FORUM_URL_F + _forumId + FORUM_URL_ORDER
				+ String.valueOf(_page);
		task.setForumId(_forumId);
		task.execute(forumUrl);
	}

	@SuppressWarnings("unchecked")
	@Override
	public void doCallback(List<Thread> result, Object... extra) {
		if (result == null || result.size() == 0) {
			Toast.makeText(this,
					"Cannot access to VozForum. Please try again later.",
					Toast.LENGTH_SHORT).show();
			return;
		}
		threads = result;
		forums = (List<Forum>) extra[0];
		lastPage = (Integer) extra[1];
		insertForumToThreads();
        GoFastEngine.notify(this, "threads");
		VozCache.instance().cache().put(FORUM_THREADS + VozCache.instance().getCurrentForum().getId(), threads);
		VozCache.instance().cache().put(FORUM_LAST_PAGE + VozCache.instance().getCurrentForum().getId(), lastPage);
		VozCache.instance().cache().put(SUB_FORUMS + VozCache.instance().getCurrentForum().getId(), forums);
		updateStatus();
	}

    private void updateNavigationPanel() {
        SegmentedGroup navigationGroup = (SegmentedGroup)findViewById(R.id.navigation_group);
        navigationGroup.removeAllViews();
        LayoutInflater mInflater = (LayoutInflater) getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
		int currentPage = VozCache.instance().getCurrentForumPage();
		if(currentPage > 3) {
			RadioButton first = (RadioButton) mInflater.inflate(R.layout.navigation_button, null);
			first.setText("<<");
			first.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					goFirst();
				}
			});
			navigationGroup.addView(first);
		}
        int prevStart = currentPage - 2;
        if(prevStart < 1) prevStart = 1;
        int extraRight = 0;
		for (int i = prevStart; i <= currentPage; i++) {
			RadioButton prevPage = (RadioButton)mInflater.inflate(R.layout.navigation_button, null);
			prevPage.setText(String.valueOf(i));
            final int page = i;
			if(i == currentPage) prevPage.setChecked(true);
			prevPage.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    goToPage(page);
                }
            });
            navigationGroup.addView(prevPage);
            extraRight++;
		}
        int nextEnd = currentPage + 2;
        if(nextEnd < 5) nextEnd = 5;
        if(nextEnd > lastPage) nextEnd = lastPage;
        for (int i = currentPage + 1; i <= nextEnd; i++) {
            RadioButton nextPage = (RadioButton)mInflater.inflate(R.layout.navigation_button, null);
            nextPage.setText(String.valueOf(i));
            final int page = i;
            nextPage.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    goToPage(page);
                }
            });
            navigationGroup.addView(nextPage);
        }
        if(nextEnd < lastPage) {
            RadioButton last = (RadioButton) mInflater.inflate(R.layout.navigation_button, null);
            last.setText(">>");
            last.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    goLast();
                }
            });
            navigationGroup.addView(last);
        }
        navigationGroup.updateBackground();
        navigationGroup.requestLayout();
        navigationGroup.invalidate();
    }

    private void insertForumToThreads() {
		List<Thread> newThreads = new ArrayList<Thread>();
		for (Forum forum : forums) {
			Thread th = new Thread();
			th.setTitle(forum.getForumName());
			th.setPoster("");
			th.setLastUpdate("");
			th.setSubForum(true);
			newThreads.add(th);
		}
		newThreads.addAll(threads);
		threads = newThreads;
	}

	@Override
	public void onItemClick(AdapterView<?> adapterView, View view,
			int selectedIndex, long arg3) {
		Thread currentThread = threads.get(selectedIndex);
		VozCache.instance().cache().put(FORUM_POSITION, selectedIndex);
		if (!currentThread.isSubForum()) {
			VozCache.instance().setCurrentThread(currentThread);
			VozCache.instance().setCurrentThreadPage(1);
            String url = VOZ_LINK + "/"
                    + currentThread.getThreadUrl()
                    + "&page="
                    + String.valueOf(VozCache.instance().getCurrentThreadPage());
            VozCache.instance().navigationList.add(url);
            Intent intent = new Intent(this, ThreadActivity.class);
			startActivity(intent);
		} else {
			VozCache.instance().setCurrentThread(null);
			Forum subForum = null;
			for (Forum forum : forums) {
				if (currentThread.getTitle().equals(forum.getForumName())) {
					subForum = forum;
					break;
				}
			}
			if (subForum != null) {
				forums = new ArrayList<Forum>();
				threads = new ArrayList<Thread>();
				GoFastEngine.notify(this, "threads");
				VozCache.instance().setCurrentParentForum(
						VozCache.instance().getCurrentForum());
				VozCache.instance().setCurrentForum(subForum);
				subForum.setParent(VozCache.instance().getCurrentParentForum());
                String forumUrl = FORUM_URL_F + subForum + FORUM_URL_ORDER + "1";
                VozCache.instance().navigationList.add(forumUrl);
                VozCache.instance().setCurrentForumPage(1);
				loadThreads(subForum.getId(), 1);
			}
		}
	}

	@Override
	public void preProcess(int position, View convertView, Object... extra) {
		Thread currentRenderItem = threads.get(position);
		View viewHeader = convertView.findViewById(R.id.subForumHeader);
		View viewSection = convertView.findViewById(R.id.forumSection);
		if (currentRenderItem.isSubForum()) {
			if (position == 0) {
				viewHeader.setVisibility(View.VISIBLE);
				TextView textView = (TextView) viewHeader
						.findViewById(R.id.forumGroupName);
				textView.setText("Sub-Forums : ");
			} else {
				viewHeader.setVisibility(View.GONE);
			}

		} else {
			if (position == 0 || threads.get(position - 1).isSubForum()) {
				viewHeader.setVisibility(View.VISIBLE);
				TextView textView = (TextView) viewHeader
						.findViewById(R.id.forumGroupName);
				textView.setText("Threads in Forum : ");
			} else {
				viewHeader.setVisibility(View.GONE);
			}
		}
		TextView title = (TextView) viewSection.findViewById(R.id.title);
		if (currentRenderItem.isSticky()) { // set red background for text
			title.setTextColor(Color.RED);
		} else {
			title.setTextColor(Color.WHITE);
		}
		title.setTextSize(TypedValue.COMPLEX_UNIT_SP, VozConfig.instance()
				.getFontSize());
		TextView poster = (TextView) viewSection.findViewById(R.id.poster);
		poster.setTextSize(TypedValue.COMPLEX_UNIT_SP, VozConfig.instance()
                .getFontSize() - 2);
    }

	public void goFirst() {
		if (VozCache.instance().getCurrentForumPage() > 1) {
			VozCache.instance().setCurrentForumPage(1);
			refresh();
		}
	}

	public void goToPage(int page) {
        VozCache.instance().setCurrentForumPage(page);
        refresh();
	}

	public void goLast() {
		int currPage = VozCache.instance().getCurrentForumPage();
		if (currPage < lastPage) {
			currPage = lastPage;
			VozCache.instance().setCurrentForumPage(currPage);
			refresh();
		}
	}

    @Override
    protected void onResume() {
        super.onResume();
        refreshActionBarIcon();
        updateNavigationPanel();
    }

	@Override
	public void onBackPressed() {
        List<String> navigationList = VozCache.instance().navigationList;
        if (navigationList.size() > 0)
            navigationList.remove(VozCache.instance().navigationList.size() - 1);

        if (VozCache.instance().getCurrentParentForum() == null) {
			overridePendingTransition(R.animator.left_slide_in,
                    R.animator.zoom_out);
            Intent intent = new Intent(this, MainActivity.class);
			intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                    | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            VozCache.instance().cache().remove(FORUM_THREADS + VozCache.instance().getCurrentForum().getId());
			VozCache.instance().cache().remove(FORUM_LAST_PAGE  + VozCache.instance().getCurrentForum().getId());
			VozCache.instance().cache().remove(FORUM_POSITION);
			VozCache.instance().setCurrentForum(null);
			VozCache.instance().setCurrentForumPage(1);
			// remove cache since we don't need it
			// start new activity and remove all finish current activity
			startActivity(intent);
			this.finish();
		} else {
			VozCache.instance().setCurrentForum(
					VozCache.instance().getCurrentParentForum());
			VozCache.instance().setCurrentForumPage(1);
			VozCache.instance().setCurrentParentForum(null);
			forums = new ArrayList<Forum>();
			threads = new ArrayList<Thread>();
			GoFastEngine.notify(this, "threads");
			if (VozCache.instance().cache().containsKey(FORUM_THREADS + VozCache.instance().getCurrentForum().getId())) {
				reloadThreadsFromCache();
			} else { // load threads
                loadThreads();
			}
		}
	}

	public int getLastPage() {
		return lastPage;
	}

	@Override
	public void refresh() {
        loadThreads();
	}

	@Override
	public void lastBreath(Exception ex) {
		ex.printStackTrace(); // in case you want to see the stacktrace in your
								// log cat output
		BugSenseHandler.sendException(ex);
	}

	protected boolean canShowPinnedMenu() {
		if (navDrawerItems != null) {
			String forumId = VozCache.instance().getCurrentForum().getId();
			String forumUrl = FORUM_URL_F + forumId + FORUM_URL_ORDER
					+ String.valueOf(VozCache.instance().getCurrentForumPage());
			Forum forum = VozCache.instance().getCurrentForum();
			NavDrawerItem item = new NavDrawerItem(forum.getForumName(),
					forumUrl);

			if (navDrawerItems.contains(item)) {
				return true;
			} else {
				return false;
			}
		}
		return false;
	}

	protected boolean canShowUnpinnedMenu() {
		if (navDrawerItems != null) {
			String forumId = VozCache.instance().getCurrentForum().getId();
			String forumUrl = FORUM_URL_F + forumId + FORUM_URL_ORDER
					+ String.valueOf(VozCache.instance().getCurrentForumPage());
			Forum forum = VozCache.instance().getCurrentForum();
			NavDrawerItem item = new NavDrawerItem(forum.getForumName(),
					forumUrl, "forum");

			if (navDrawerItems.contains(item)) {
				return false;
			} else {
				return true;
			}
		}
		return true;
	}

	protected void doPin() {
		String forumId = VozCache.instance().getCurrentForum().getId();
		String forumUrl = FORUM_URL_F + forumId + FORUM_URL_ORDER
				+ String.valueOf(VozCache.instance().getCurrentForumPage());
		Forum forum = VozCache.instance().getCurrentForum();
		NavDrawerItem item = new NavDrawerItem(forum.getForumName(), forumUrl,
				"forum");
		item.tag = VozCache.instance().getCurrentForum();
		item.page = VozCache.instance().getCurrentForumPage();

		//pinPage(item);
		pinMenu.setVisible(true);
		unpinMenu.setVisible(false);
	}

	protected void doUnpin() {
		String forumId = VozCache.instance().getCurrentForum().getId();
		String forumUrl = FORUM_URL_F + forumId + FORUM_URL_ORDER
				+ String.valueOf(VozCache.instance().getCurrentForumPage());
		Forum forum = VozCache.instance().getCurrentForum();
		NavDrawerItem item = new NavDrawerItem(forum.getForumName(), forumUrl,
				"forum");

		//unpinPage(item);
		pinMenu.setVisible(false);
		unpinMenu.setVisible(true);
	}
}
