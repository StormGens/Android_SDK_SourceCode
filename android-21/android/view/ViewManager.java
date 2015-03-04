/*
 * Copyright (C) 2006 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package android.view;

/** 
 * 用来在Activity中添加和移除View的接口，要获得该接口的实例，调用Context.getSystemService()方法。
 * 
 * * * * Interface to let you add and remove child views to an Activity. To get an instance
  * of this class, call {@link android.content.Context#getSystemService(java.lang.String) Context.getSystemService()}.
  */
public interface ViewManager
{
    /**
     * Assign the passed LayoutParams to the passed View and add the view to the window.
     * 将传入的LayoutParams分配给传入的View，并且把这个View传入到Window中。
     * <p>在某些编程错误时，抛出android.view.WindowManager.BadTokenException异常，比如：在没有移除第一个View
     * 的时候就有添加了第二个View
     * <p>如果该Window在一个辅助显示屏上，但是辅助显示屏不能被找到，则抛出android.view.WindowManager.InvalidDisplayException
     * * *
     * * * 
     * * * 
     * * *
     * <p>Throws {@link android.view.WindowManager.BadTokenException} for certain programming
     * errors, such as adding a second view to a window without removing the first view.
     * <p>Throws {@link android.view.WindowManager.InvalidDisplayException} if the window is on a
     * secondary {@link Display} and the specified display can't be found
     * (see {@link android.app.Presentation}).
     * @param view The view to be added to this window. 要被添加到该window的view
     * @param params The LayoutParams to assign to view. 要被分配给该view的LayoutParams
     */
    public void addView(View view, ViewGroup.LayoutParams params);
    public void updateViewLayout(View view, ViewGroup.LayoutParams params);
    public void removeView(View view);
}
