package d2d.testing.gui;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import java.util.ArrayList;
import java.util.List;

public class ViewPagerAdapter extends FragmentPagerAdapter {

    private final List<Fragment> fragmentList = new ArrayList<>();
    private final List<String> fragmentListTitles = new ArrayList<>();

    public ViewPagerAdapter(FragmentManager fm){
        super(fm);

    }

    @Override
    public Fragment getItem(int i) {
        return fragmentList.get(i);
    }

    @Override
    public int getCount() {
        return fragmentListTitles.size();
    }

    @Override
    public CharSequence getPageTitle(int i){
        return fragmentListTitles.get(i);
    }

    public void AddFragment(Fragment fragment,String Title){
        fragmentList.add(fragment);
        fragmentListTitles.add(Title);
    }

    //TODO : this is really hardcoded, maybe make it more dimanic in the future
    public void setStreamNumber(int number) {
        if(number != 0)
            this.fragmentListTitles.set(1,"Streams Available" + " (" + number + ")");
        else
            this.fragmentListTitles.set(1,"Streams Available" );
    }
}
