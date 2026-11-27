package xiao.bu.tv;
import android.os.Bundle;
import xiao.bu.tv.skin.TvUi;

/** One settings model for every theme; the renderer owns only its appearance. */
final class TvUiSettingsMenu implements TvUi.Events {
    interface Host {Bundle state();Bundle groups();void group(String id,boolean visible);void setting(String key,Object value);void source(int index);void navigate(String destination);}
    private final Host host;private final TvUiMenu menu;
    static final int BROWSER_CATEGORY=3;
    private Bundle groupChoices;
    // [lwwy-mod] Hidden settings entries. The menu is built from these arrays, so a
    // hidden category is removed from both keys[] and titles[] and all downstream
    // option indices are remapped in options()/action() below. Functionality is
    // only hidden from the UI, not disabled in the app.
    private static final String[] keys={"source","decode","channels","display"};
    private static final String[] titles={"线路选择","解码方式","频道管理","显示设置"};
    TvUiSettingsMenu(Host host,TvUiMenu menu){this.host=host;this.menu=menu;}
    Bundle initial(){Bundle b=new Bundle();b.putStringArray("categories",titles);return b;}
    private static Bundle choices(String[] items,int selected){Bundle b=new Bundle();b.putStringArray("items",items);b.putInt("selected",selected);return b;}
    private static String check(String title,boolean checked){return title+(checked?" ●":" ○");}
    public Bundle options(int category){Bundle s=host.state();if(category<0||category>=keys.length)return choices(new String[0],-1);String key=keys[category];
        if("source".equals(key)){int count=s.getInt("sources");String[] names=s.getStringArray("sourceNames");String[] rows=new String[count];for(int i=0;i<count;i++)rows[i]=names!=null&&i<names.length?names[i]:"线路"+(i+1);return choices(rows,s.getInt("source"));}
        if("ratio".equals(key)){String mode=s.getString("ratio");return choices(android.os.Build.VERSION.SDK_INT>=24?new String[]{"原始比例","全屏显示","18:9","16:9","4:3","裁剪","背景模糊"}:new String[]{"原始比例","全屏显示","18:9","16:9","4:3","裁剪"},"stretch".equals(mode)?1:"18:9".equals(mode)?2:"16:9".equals(mode)?3:"4:3".equals(mode)?4:"fill".equals(mode)?5:"blur".equals(mode)&&android.os.Build.VERSION.SDK_INT>=24?6:0);}
        if("decode".equals(key)){String mode=s.getString("decode");return choices(new String[]{"智能硬解","硬件解码","软件解码"},"hardware".equals(mode)?1:"software".equals(mode)?2:0);}
        if("timeout".equals(key))return choices(new String[]{"关闭","5 秒换源","10 秒换源","15 秒换源","20 秒换源","25 秒换源","30 秒换源"},SourceSwitchTimeout.valid(s.getInt("timeout"))?s.getInt("timeout")/5:0);
        if("channels".equals(key)){
            groupChoices=host.groups();String[] names=groupChoices.getStringArray("names");boolean[] shown=groupChoices.getBooleanArray("visible");
            String[] rows=new String[names.length];for(int i=0;i<names.length;i++)rows[i]=check(names[i],shown[i]);
            return choices(rows,-1);
        }
        // [lwwy-mod] display: keep only 显示网速/调试信息.
        if("display".equals(key))return choices(new String[]{check("显示网速",s.getBoolean("speed")),check("调试信息",s.getBoolean("debug"))},-1);
        // [lwwy-mod] preferences: keep only 界面主题.
        if("preferences".equals(key))return choices(new String[]{"界面主题"},-1);
        // [lwwy-mod] version: show build number only; 检查更新 hidden.
        Bundle versionChoices=choices(new String[]{s.getString("version")},-1);
        versionChoices.putInt("firstEnabled",1);
        return versionChoices;
    }
    public void action(int category,int option){if(category<0||category>=keys.length)return;Bundle s=host.state();String key=keys[category];
        if("channels".equals(key)){
            if(groupChoices==null)options(category);String[] ids=groupChoices.getStringArray("ids");boolean[] shown=groupChoices.getBooleanArray("visible");
            if(option>=0&&option<ids.length)host.group(ids[option],!shown[option]);return;
        }
        if(option<0||option>=("source".equals(key)?s.getInt("sources"):options(category).getStringArray("items").length))return;
        if("source".equals(key)){menu.dismiss();host.source(option);}
        else if("ratio".equals(key))host.setting("videoScaleMode",new String[]{"fit","stretch","18:9","16:9","4:3","fill","blur"}[option]);
        else if("decode".equals(key))host.setting("decodeMode",new String[]{"auto","hardware","software"}[option]);
        else if("timeout".equals(key))host.setting("autoSwitchSourceSeconds",option*5);
        // [lwwy-mod] remapped: display keeps only 0=网速,1=调试.
        else if("display".equals(key)){if(option==0)host.setting("showNetworkSpeed",!s.getBoolean("speed"));else if(option==1)host.setting("showDebugInfo",!s.getBoolean("debug"));}
        // [lwwy-mod] remapped: only 界面主题 survives.
        else if("preferences".equals(key)){if(option==0)go("styles");}
        // [lwwy-mod] version: 检查更新 hidden, no clickable action remains.
        else if("browser".equals(key))go("browser:"+option);
    }
    private void go(String where){menu.dismiss();host.navigate(where);}
    public void dismiss(){menu.dismiss();}
}
