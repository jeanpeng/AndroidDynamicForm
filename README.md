# dynamicForm
基于xml配置的Android动态表单，支持checkbox和radiobox多选一,支持选项之间的关联  

xml模板格式
```
<ruleItem name="性别" btnType="radiobox" hideLine="true">
    <Item name="男" default="true">
	<params key="sex" type="int">4</params>
    </Item>
    <Item name="女">
	<params key="sex" type="int">3</params>
    </Item>
</ruleItem>
<ruleItem name="" btnType="checkbox">
    <Item name="全日制">
        <params checked="2" key="qrz" type="int">1</params>
    </Item>
</ruleItem>
```
ruleItem：选项组  
Item：子选项  
name:展示类别  
btnType：btn的类型  
hideLine：是否隐藏分隔线  
params：参数  
condition: 关联参数  

![Sample Pic](https://raw.githubusercontent.com/jeanpeng/AndroidDynamicForm/master/demo_img.jpg)

