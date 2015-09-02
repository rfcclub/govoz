package com.gotako.gofast;

import static com.gotako.gofast.bind.InputBindingHelper.bindInput;
import static com.gotako.gofast.bind.OutputBindingHelper.bindOutput;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.app.Activity;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.TextView;

import com.gotako.gofast.adapter.BindingAdapter;
import com.gotako.gofast.adapter.ExpandableAdapter;
import com.gotako.gofast.annotation.BindingCollection;
import com.gotako.gofast.annotation.BindingField;
import com.gotako.gofast.utils.BindUtil;
import com.gotako.gofast.utils.ObjectUtil;
import com.gotako.gofast.utils.ReflectionUtil;

public class GoFastEngine {
	private static GoFastEngine engine;

	public Map<String, BindingObject> bindingCache = new HashMap<String, BindingObject>();
	public Activity currentActivity;

	/**
	 * Get singleton instance of GoFastEngine.
	 * 
	 * @return
	 */
	public static GoFastEngine instance() {
		if (engine == null) {
			engine = new GoFastEngine();
		}
		return engine;
	}

	/**
	 * Static convenience method to scan activity for binding annotation
	 * 
	 * @param source
	 *            source activity
	 */
	public static void initialize(Activity source) {
		instance().scan(source);
	}
	
	public static void populateControls(Activity source) {
		instance().doPopulateControls(source);
	}
	
	private void doPopulateControls(Activity source) {
		Field[] fields = source.getClass().getDeclaredFields();
		for(Field field : fields) {
			Control control = field.getAnnotation(Control.class);
			if(control != null) {
				if(!field.isAccessible()) field.setAccessible(true);
				try {
					field.set(source, source.findViewById(control.id()));
				} catch (IllegalArgumentException e) {					
					// do nothing
				} catch (IllegalAccessException e) {
					// do nothing					
				}				
			}
		}
	}

	/**
	 * Static convenience method to notify change from a field of an object
	 * 
	 * @param source
	 *            source object
	 * @param fieldName
	 *            name of field which needs to notify
	 */
	public static void notify(Object source, String fieldName) {
		instance().notifyFieldChange(source, fieldName);
	}

	/**
	 * Notify field change from object with field name
	 * 
	 * @param source
	 *            source
	 * @param fieldName
	 *            name of field
	 */
	public void notifyFieldChange(Object source, String fieldName) {
		doNotifyFieldOnSource(source, fieldName, false, null);
	}

	/**
	 * Notify field change from object with field name and value
	 * 
	 * @param source
	 *            object source
	 * @param fieldName
	 *            name of field need to notify
	 * @param value
	 *            value of notify field
	 */
	public void notifyFieldChange(Object source, String fieldName, Object value) {
		doNotifyFieldOnSource(source, fieldName, true, value);
	}

	private void doNotifyFieldOnSource(Object source, String fieldName,
			boolean hasPassedValue, Object passedValue) {
		BindingObject bObj = bindingCache.get(BindUtil.getBindKey(source,
				fieldName));
		if (bObj == null)
			return;
		Object value = passedValue;
		if (!hasPassedValue) {
			value = ReflectionUtil.getValue(bObj.getSource(), bObj.getField(),
					true);
		}
		doNotifyFieldChange(bObj, value);

	}

	/**
	 * Notify field change from active activity with field name and value
	 * 
	 * @param fieldName
	 *            name of field need to notify
	 * @param value
	 *            value of notify field
	 */
	public void notifyFieldChangeCurrent(String fieldName, Object value) {
		doNotifyFieldOnActivity(fieldName, true, value);
	}

	/**
	 * Notify field change from active activity with field name only
	 * 
	 * @param fieldName
	 *            name of field need to notify
	 */
	public void notifyFieldChangeCurrent(String fieldName) {
		doNotifyFieldOnActivity(fieldName, false, null);
	}

	private void doNotifyFieldOnActivity(String fieldName,
			boolean hasPassedValue, Object passedValue) {
		Activity activity = getCurrentActivity();
		if (activity == null)
			return; // if current activity is null return ( do nothing)
		BindingObject bObj = bindingCache.get(BindUtil.getBindKey(activity,
				fieldName));
		if (bObj == null)
			return; // if cannot find binding object so return
		Object value = passedValue;
		if (!hasPassedValue) {
			value = ReflectionUtil.getValue(bObj.getSource(), bObj.getField(),
					true);
		}
		doNotifyFieldChange(bObj, value);
	}

	@SuppressWarnings("rawtypes")
	private void doNotifyFieldChange(BindingObject bObj, Object value) {
		bObj.setValue(value);
		if (bObj instanceof ReactiveField) {
			((ReactiveField) bObj).raiseFieldChangeEvent();
		} else if (bObj instanceof ReactiveCollectionField) {
			final ReactiveCollectionField rcf = ((ReactiveCollectionField) bObj);
			final BindingAdapter adapter = rcf.getAdapter();
			if (adapter instanceof ExpandableAdapter) {
				rcf.setValue(value);
				rcf.bind(rcf.getView());
			} else {
				adapter.setDataSource(value);
			}
			// do notify on UI thread			
			((Activity)rcf.getContext()).runOnUiThread(new Runnable() {
				@Override
				public void run() {
					
					adapter.notifyDataSetChanged();
					rcf.getView().invalidate();
				}				
			});
						
		} else {			
			setValueOnView(bObj.getView(), value);
		}
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public boolean bind(Activity model, String fieldName, int resourceId,
			boolean twoWay) {
		String key = BindUtil.getBindKey(model, fieldName);
		try {
			Field field = model.getClass().getDeclaredField(fieldName);
			Class clazz = field.getType();
			if (clazz.isArray() || clazz.isAssignableFrom(Collection.class)) {
				// do nothing
			} else {
				// check in cache before create new ReactiveField
				ReactiveField reactField = null;
				if (bindingCache.get(key) == null) { // if key is not exists
														// create new key
					reactField = new ReactiveField(model, fieldName);
					bindingCache.put(key, reactField);
				} else { // update current ReactiveField only
					reactField = (ReactiveField) bindingCache.get(key);
					reactField.setSource(model);
				}
				View view = model.findViewById(resourceId);
				bindControl(reactField, view, twoWay);
			}
			return true;
		} catch (Exception e) {
			// remove cache key if added
			bindingCache.remove(key);
			return false;
		}
	}

	@SuppressWarnings("unchecked")
	public <T> boolean bindCollection(Activity source, String fieldName,
			int resourceId, int layoutId, int groupId, Object dataSource) {
		String key = BindUtil.getBindKey(source, fieldName);
		try {
			ReactiveCollectionField<T> field = null;
			if (bindingCache.get(key) == null) { // if key is not exists
				// create new key
				if (groupId == 0) {
					field = new ReactiveCollectionField<T>(
						source, fieldName, layoutId);
				} else {
					field = new ReactiveCollectionField<T>(
							source, fieldName, layoutId, groupId);
				}
				bindingCache.put(key, field);
			} else { // update current ReactiveField only
				field = (ReactiveCollectionField<T>) bindingCache.get(key);
				field.setSource(source);
			}
			
			field.setValue(dataSource);
			field.bind(source.findViewById(resourceId));
			return true;
		} catch (RuntimeException e) {
			// remove added key to reduce memory
			bindingCache.remove(key);
			return false;
		}
	}

	public void scan(Activity source) {
		Field[] fields = source.getClass().getDeclaredFields();
		for (Field field : fields) {
			if (parseBindingField(source, field))
				continue;
			if (parseBindingCollection(source, field))
				continue;
		}
	}

	private boolean parseBindingCollection(Activity source, Field field) {
		BindingCollection bindingCollection = field
				.getAnnotation(BindingCollection.class);
		if (bindingCollection == null)
			return false;
		Object dataSource = ReflectionUtil.getValue(source, field, true);
		// if data source is null or not Collection data type
		if (dataSource == null
				|| dataSource.getClass().isAssignableFrom(List.class)) {
			return false;
		}
		bindCollection(source, field.getName(), bindingCollection.id(),
					bindingCollection.layout(), bindingCollection.groupLayout(),
					dataSource);

		return true;
	}

	private boolean parseBindingField(Activity source, Field field) {
		BindingField bindingField = field.getAnnotation(BindingField.class);
		if (bindingField == null)
			return false;
		bind(source, field.getName(), bindingField.id(), bindingField.twoWay());
		return true;

	}

	/**
	 * @param source
	 *            source object contains field name
	 * 
	 * @param fieldName
	 *            field name which needs to be binded
	 * @param activity
	 *            activity contains view
	 * @param viewId
	 *            id of the binding view
	 * @param twoWayBinding
	 *            If false, View changes update the ViewModel only. If true,
	 *            View changes update the ViewModel and also ViewModel change
	 *            will update the View.
	 */
	public void bindControl(Object source, String fieldName, Activity activity,
			int viewId, boolean twoWayBinding) {
		ReactiveField field = new ReactiveField(source, fieldName);
		View view = activity.findViewById(viewId);
		bindControl(field, view, twoWayBinding);
	}

	/**
	 * Bind control in View to a field of ViewModel. ViewModel's field is
	 * wrapped in a BindingObject.
	 * 
	 * @param field
	 *            field to wrap
	 * @param activity
	 *            activity to get view from view id
	 * @param viewId
	 *            view id
	 * @param twoWayBinding
	 *            If false, View changes update the ViewModel only. If true,
	 *            View changes update the ViewModel and also ViewModel change
	 *            will update the View.
	 */
	public void bindControl(BindingObject field, Activity activity, int viewId,
			boolean twoWayBinding) {
		View view = activity.findViewById(viewId);
		bindControl(field, view, twoWayBinding);
	}

	/**
	 * Bind control in View to a field of ViewModel. ViewModel's field is
	 * wrapped in a BindingObject.
	 * 
	 * @param field
	 *            reactive field contains field in ViewModel
	 * @param view
	 *            View needs to bind
	 * @param twoWayBinding
	 *            If false, View changes update the ViewModel only. If true,
	 *            View changes update the ViewModel and also ViewModel change
	 *            will update the View.
	 */
	public void bindControl(BindingObject field, View view,
			boolean twoWayBinding) {
		field.setView(view);
		bindInput(view, field);
		if (twoWayBinding) {
			bindOutput(view, field);
		}
	}

	/**
	 * Set value from ViewModel to View
	 * 
	 * @param view
	 *            View to set value
	 * @param value
	 *            value need to set
	 */
	public void setValueOnView(View view, Object value) {
		if (view instanceof TextView) {
			TextView text = (TextView) view;
			// String stringValue = (String) value;
			// text.setText(stringValue);
			text.setText(ObjectUtil.parseString(value));
		} else if (view instanceof EditText) {
			EditText text = (EditText) view;
			// text.setText((String) value);
			text.setText(ObjectUtil.parseString(value));
		} else if (view instanceof CompoundButton) { // CheckBox,RadioButton,ToggleButton,Switch
			Boolean boo = ObjectUtil.parseBoolean(value);
			if (boo != null) {
				((CompoundButton) view).setChecked(boo);
			}
		}
	}

	public void setValueOnField(BindingObject field, View view) {
		if (view instanceof TextView) {
			field.setValue(((TextView) view).getText());

		} else if (view instanceof EditText) {
			field.setValue(((EditText) view).getText());
		} else if (view instanceof CheckBox) {
			field.setValue(((CheckBox) view).isChecked());
		}
	}

	public void bindObject(ReactiveObject object, View layoutView) {

	}

	public Activity getCurrentActivity() {
		return currentActivity;
	}

	public void setCurrentActivity(Activity currentActivity) {
		this.currentActivity = currentActivity;
	}

	public BindingObject getBindingObject(Activity context, String property) {
		String key = BindUtil.getBindKey(context, property);
		return bindingCache.get(key);
	}
	
	@SuppressWarnings("unchecked")
	public <T extends BindingObject> T getBindingObject(Activity context,String property, Class<T> clazz) {
		String key = BindUtil.getBindKey(context, property);
		return (T)bindingCache.get(key);
	}
}
