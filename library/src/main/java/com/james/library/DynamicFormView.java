package com.james.library;

import android.content.Context;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.james.library.utils.DipPixelUtil;
import com.james.library.utils.IdUtil;
import com.james.library.utils.StringUtil;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Create by james on 2018/9/3
 */
public class DynamicFormView extends RelativeLayout{
    private Map<String, Object> mCreateRuleMap = new HashMap<String, Object>();
    private static final int TEXT_MARGIN_LEFT = 17;
    private static final int ITEM_GROUP_MARGIN_LEFT = 62;
    private static final int SECOND_OPTION_MARGIN_LEFT = 210;
    private static final int LINE_SPACE_MARGIN = 40;
    private int mItemGroupMarginTop = 13;
    private Map mRadioGroupSelectIndexMap = new HashMap();
    private Map<String, String> mCostCoinsMap = new HashMap<String, String>();
    private Context mContext;
    private Button mSaveBtn;
    private RelativeLayout mRootLayout;

    public DynamicFormView(Context context) {
        this(context,null);
    }

    public DynamicFormView(Context context, AttributeSet attrs) {
        this(context,attrs,0);
    }

    public DynamicFormView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context,attrs,defStyleAttr);
        mContext = context;
        initUI();
    }

    private void initUI() {
        View view = inflate(mContext, R.layout.layout_dynamic_form, this);
        mRootLayout = view.findViewById(R.id.layout_root);
        mSaveBtn = view.findViewById(R.id.btn_save);
        mSaveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // do something
            }
        });
        doLayout();
    }



    /**
     * 根据配置文件动态布局
     */
    private void doLayout() {
        try {
            SAXReader reader = new SAXReader();
            Document document = reader.read(mContext.getAssets().open("ezhouhh.xml"));
            Element root = document.getRootElement();
            Iterator<Element> itor = root.elementIterator();
            while (itor.hasNext()) {
                Element ruleElement = itor.next();
                // 判断rule是否需要展示
                Element ruleCondition = ruleElement.element("condition");
                if (ruleCondition != null && !ruleCondition.getText().equals(String.valueOf(mCreateRuleMap.get(ruleCondition.attributeValue("key"))))) {
                    continue;
                }
                String name = ruleElement.attributeValue("name");
                if (!StringUtil.isEmpty(name)) {
                    //类目名称
                    TextView typeNameTextView = new TextView(mContext);
                    typeNameTextView.setText(name);
                    typeNameTextView.setTextColor(mContext.getResources().getColor(R.color.color_000000));
                    typeNameTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
                    LayoutParams layoutParams = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
                    layoutParams.setMargins(DipPixelUtil.dip2px(mContext, TEXT_MARGIN_LEFT), DipPixelUtil.dip2px(mContext, mItemGroupMarginTop), 0, 0);
                    typeNameTextView.setLayoutParams(layoutParams);
                    mRootLayout.addView(typeNameTextView);
                }
                String btnType = ruleElement.attributeValue("btnType");
                List<Element> itemList = ruleElement.elements("Item");
                if (btnType.equals("radiobox")) {
                    Iterator<Element> itorItem = itemList.iterator();
                    while (itorItem.hasNext()) {
                        Element item = itorItem.next();
                        //判断Item是否展示
                        List <Element> conditions = item.elements("condition");
                        for(Element condition:conditions){
                            if (condition != null && !condition.getText().equals(String.valueOf(mCreateRuleMap.get(condition.attributeValue("key"))))) {
                                itorItem.remove();
                                break;
                            }
                        }
                    }
                    layoutRadioGroup(itemList);
                } else if (btnType.equals("checkbox")) {
                    layoutCheckBox(itemList);
                }
                boolean hideline = ruleElement.attribute("hideLine") != null ? Boolean.parseBoolean(ruleElement.attributeValue("hideLine")) : false;
                if (!hideline) {
                    View lineView = new View(mContext);
                    lineView.setBackgroundColor(mContext.getResources().getColor(R.color.color_E6E6E6));
                    LayoutParams layoutParams = new LayoutParams(LayoutParams.MATCH_PARENT, 2);
                    layoutParams.setMargins(DipPixelUtil.dip2px(mContext, 64), DipPixelUtil.dip2px(mContext, mItemGroupMarginTop), 0, 0);
                    lineView.setLayoutParams(layoutParams);
                    mRootLayout.addView(lineView);
                    mItemGroupMarginTop += 5;
                }
            }

        } catch (DocumentException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String getCheckItemValue(List<Element> optionsList) {
        Element mainElement = (Element) optionsList.get(0).elements("params").get(0);
        String paramsName = mainElement.attributeValue("key");
        String ruleValue = String.valueOf(mCreateRuleMap.get(paramsName));
        boolean containRuleValue = false;
        String firstItemValue = "";
        String defaultValue = "";
        for (int i = 0; i < optionsList.size(); i++) {
            Element element = optionsList.get(i);
            Element mElement = (Element) element.elements("params").get(0);
            if (i == 0) {
                firstItemValue = mElement.getText();
            }
            if (ruleValue != null && mElement.getText().equals(ruleValue)) {
                containRuleValue = true;
                break;
            }
            if (element.attribute("default") != null && Boolean.parseBoolean(element.attributeValue("default"))) {
                defaultValue = mElement.getText();
            }
        }
        if (containRuleValue) {
            return ruleValue;
        } else {
            if (!StringUtil.isEmpty(defaultValue)) {
                return defaultValue;
            } else {
                return firstItemValue;
            }
        }

    }

    /**
     * 根据节点类型判断参数的值
     *
     * @param paramElement
     * @return
     */
    private Object getParamValue(Element paramElement) {
        if (paramElement.attribute("type") != null && paramElement.attributeValue("type").equals("int")) {
            return Integer.parseInt(paramElement.getText());
        }
        return paramElement.getText();
    }

    /**
     * 根据节点类型判断参数的值
     *
     * @param paramElement
     * @return
     */
    private Object getParamCheckedValue(Element paramElement) {
        if (paramElement.attribute("type") != null && paramElement.attributeValue("type").equals("int")) {
            return Integer.parseInt(paramElement.attributeValue("checked"));
        }
        return paramElement.attributeValue("checked");
    }


    /**
     * 布局radio类型
     *
     * @param optionsList
     */
    private void layoutRadioGroup(List<Element> optionsList) {
        if (optionsList.size() > 0) {
            final String paramsName = ((Element) optionsList.get(0).elements("params").get(0)).attributeValue("key");
            int size = optionsList.size();
            final int radioCount = 2;
            int mod = size % radioCount;
            int radioGroupCount = 0 == mod ? size / radioCount : size / radioCount + 1;
            RadioGroup.LayoutParams radioParams = new RadioGroup.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT, 1);
            RadioGroup radioGroup = null;
            String checkItemValue = getCheckItemValue(optionsList);
            for (int i = 0; i < radioGroupCount; i++) {
                LayoutParams groupParams = new LayoutParams(LayoutParams.MATCH_PARENT,
                        LayoutParams.WRAP_CONTENT);
                groupParams.setMargins(DipPixelUtil.dip2px(mContext, ITEM_GROUP_MARGIN_LEFT), DipPixelUtil.dip2px(mContext, mItemGroupMarginTop), 0, 0);
                mItemGroupMarginTop = mItemGroupMarginTop + LINE_SPACE_MARGIN;
                radioGroup = new RadioGroup(mContext);
                radioGroup.setTag(paramsName);
                radioGroup.setLayoutParams(groupParams);
                radioGroup.setOrientation(RadioGroup.HORIZONTAL);
                radioGroup.setWeightSum(radioCount);
                int radioMaxSize = (i + 1) * radioCount;
                for (int j = i * radioCount; j < radioMaxSize && j < size; j++) {
                    boolean checked = false;
                    Element itemElement = optionsList.get(j);
                    List<Element> paramsElement = itemElement.elements("params");
                    if (paramsElement.get(0).getText().equals(checkItemValue)) {
                        checked = true;
                        for (Element e : paramsElement) {
                            mCreateRuleMap.put(e.attributeValue("key"), getParamValue(e));
                        }
                        mRadioGroupSelectIndexMap.put(paramsName, i);
                    }
                    //保存coins的值
                    if (itemElement.attribute("coins") != null) {
                        String coins = itemElement.attributeValue("coins");
                        String paramValue = ((Element) itemElement.elements("params").get(0)).getText();
                        String[] coinArray = coins.split(",");
                        for (int k = 0; k < coinArray.length; k++) {
                            mCostCoinsMap.put(paramValue + "_" + (k + 1), coinArray[k]);
                        }
                    }

                    RadioButton radio = new RadioButton(mContext);
                    radio.setId(IdUtil.generateViewId());
                    radio.setLayoutParams(radioParams);
                    radio.setText(itemElement.attributeValue("name"));
                    //radio.setTag(new RadioInfo(i, getParamValue((Element) itemElement.elements("params").get(0))));
                    radio.setTag(new RadioInfo(i, itemElement.elements("params")));
                    radio.setButtonDrawable(R.drawable.selector_radiobtn);
                    radio.setPadding(14, 0, 0, 0);
                    radio.setChecked(checked);
                    radioGroup.addView(radio);
                }
                radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(RadioGroup group, int checkedId) {
                        RadioButton radioButton = (RadioButton) group.findViewById(checkedId);
                        if (null != radioButton && radioButton.isChecked()) {
                            // 首次选中不需要重置各个radioGroup的状态
                            RadioInfo radioInfo = (RadioInfo) radioButton.getTag();
                            if (radioInfo.groupIndex != (int) mRadioGroupSelectIndexMap.get(paramsName)) {
                                resetUnselectedRadioGroupStatus(paramsName, radioInfo.groupIndex);
                            }
                            mRadioGroupSelectIndexMap.put(paramsName, radioInfo.groupIndex);
                            for (Element e : (List<Element>)radioInfo.value) {
                                mCreateRuleMap.put(e.attributeValue("key"), getParamValue(e));
                            }
                            //mCreateRuleMap.put(paramsName, radioInfo.value);
                            mRootLayout.removeAllViews();
                            mItemGroupMarginTop = 13;
                            doLayout();

                        }
                    }


                });
                mRootLayout.addView(radioGroup);
            }
        }
    }


    class RadioInfo {
        public int groupIndex;
        public Object value;

        public RadioInfo(int index, Object value) {
            this.groupIndex = index;
            this.value = value;
        }
    }

    /***
     *
     * @description 恢复未选中的radioGroup状态
     */
    private void resetUnselectedRadioGroupStatus(String tag, int radioGroupIndex) {
        if (null != mRootLayout) {
            int size = mRootLayout.getChildCount();
            int j = 0;
            for (int i = 0; i < size; i++) {
                View view = mRootLayout.getChildAt(i);
                if (null != view && view instanceof RadioGroup && view.getTag().equals(tag)) {
                    if (j != radioGroupIndex) {
                        ((RadioGroup) view).clearCheck();
                    }
                    j++;
                }
            }
        }
    }

    /**
     * 布局checkbox类型
     */
    private void layoutCheckBox(List<Element> optionsList) {
        for (int i = 0; i < optionsList.size(); i++) {
            final Element element = optionsList.get(i);
            CheckBox checkBox = new CheckBox(mContext);
            checkBox.setText(element.attributeValue("name"));
            checkBox.setButtonDrawable(R.drawable.selector_checkbox);
            LayoutParams layoutParams = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
            //换行
            if (i % 2 == 0 && i != 0) {
                mItemGroupMarginTop += LINE_SPACE_MARGIN;
            }
            layoutParams.setMargins(DipPixelUtil.dip2px(mContext, i % 2 == 0 ? ITEM_GROUP_MARGIN_LEFT : SECOND_OPTION_MARGIN_LEFT), DipPixelUtil.dip2px(mContext, mItemGroupMarginTop), 0, 0);
            checkBox.setLayoutParams(layoutParams);
            final List<Element> paramElements = element.elements("params");
            // 一个checkbox下有多个param的时候，根据第一个param判断是否选中
            final String ruleKey = paramElements.get(0).attributeValue("key");
            boolean isChecked = false;
            if (paramElements.get(0).attribute("default") != null) {
                isChecked = Boolean.parseBoolean(paramElements.get(0).attributeValue("default"));
            }

            final String defaultValue = paramElements.get(0).getText();
            final String checkedValue = paramElements.get(0).attributeValue("checked");
            if (mCreateRuleMap.get(ruleKey) == null) {
                checkBox.setChecked(isChecked);
                for (Element e : paramElements) {
                    mCreateRuleMap.put(e.attributeValue("key"), isChecked ? getParamCheckedValue(e) : getParamValue(e));

                }
            } else {
                Object value = mCreateRuleMap.get(ruleKey);
                String strValue = null;
                if (value instanceof Double) {
                    strValue = String.valueOf((int) value);
                } else {
                    strValue = String.valueOf(value);
                }
                checkBox.setChecked(strValue.equals(checkedValue));
            }
            mRootLayout.addView(checkBox);
            checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    if (isChecked) {
                        for (Element e : paramElements) {
                            mCreateRuleMap.put(e.attributeValue("key"), getParamCheckedValue(e));
                        }
                    } else {
                        for (Element e : paramElements) {
                            mCreateRuleMap.put(e.attributeValue("key"), getParamValue(e));
                        }
                    }
                }
            });
        }
        mItemGroupMarginTop += LINE_SPACE_MARGIN;
    }

}
