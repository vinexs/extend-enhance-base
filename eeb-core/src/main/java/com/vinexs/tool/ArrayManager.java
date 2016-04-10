/*
 * Copyright (c) 2015. Vin @ vinexs.com (MIT License)
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package com.vinexs.tool;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@SuppressWarnings("unused")
public class ArrayManager {

    // Push
    public static String[] push(String[] array, String newString) {
        String temp[] = new String[array.length + 1];
        System.arraycopy(array, 0, temp, 0, array.length);
        temp[array.length] = newString;
        return temp;
    }

    public static String[] push(String[] array, String[] newArray) {
        String temp[] = new String[array.length + newArray.length];
        System.arraycopy(array, 0, temp, 0, array.length);
        System.arraycopy(newArray, 0, temp, array.length, newArray.length);
        return temp;
    }

    public static String[] push(String[] array, ArrayList<String> newArray) {
        String temp[] = new String[array.length + newArray.size()];
        System.arraycopy(array, 0, temp, 0, array.length);
        for (int i = 0; i < newArray.size(); i++) {
            temp[i + array.length] = newArray.get(i);
        }
        return temp;
    }

    public static int[] push(int[] array, int newInt) {
        int temp[] = new int[array.length + 1];
        System.arraycopy(array, 0, temp, 0, array.length);
        temp[array.length] = newInt;
        return temp;
    }

    public static int[] push(int[] array, int[] newArray) {
        int temp[] = new int[array.length + newArray.length];
        System.arraycopy(array, 0, temp, 0, array.length);
        System.arraycopy(newArray, 0, temp, array.length, newArray.length);
        return temp;
    }

    public static int[] push(int[] array, ArrayList<Integer> newArray) {
        int temp[] = new int[array.length + newArray.size()];
        System.arraycopy(array, 0, temp, 0, array.length);
        for (int i = 0; i < newArray.size(); i++) {
            temp[i + array.length] = newArray.get(i);
        }
        return temp;
    }

    // Join
    public static String join(String[] array) {
        return join(array, ",");
    }

    public static String join(String[] array, String spilter) {
        return Arrays.asList(array).toString().replace(", ", spilter).replaceAll("(\\[|\\])", "");
    }

    public static CharSequence join(CharSequence[] array) {
        return join(array, ",");
    }

    public static String join(CharSequence[] array, String spilter) {
        return Arrays.asList(array).toString().replace(", ", spilter).replaceAll("(\\[|\\])", "");
    }

    public static String join(int[] array) {
        return join(array, ",");
    }

    public static String join(int[] array, String spilter) {
        return Arrays.asList(array).toString().replaceAll(", ", spilter).replaceAll("(\\[|\\])", "");
    }

    public static <E> String join(ArrayList<E> array) {
        return join(array, ",");
    }

    public static <E> String join(ArrayList<E> array, String spilter) {
        String result = "";
        if (array.size() == 0) {
            return result;
        }
        for (E i : array) {
            if (result.equals("")) {
                result += i + "";
            } else {
                result += spilter + i;
            }
        }
        return result.replaceAll("(\\[|\\])", "");
    }

    // Reverse
    public static String[] reverse(String[] array) {
        String temp[] = new String[array.length];
        for (int i = 0; i < array.length; i++) {
            temp[i] = array[(array.length - 1) - i];
        }
        return temp;
    }

    public static CharSequence[] reverse(CharSequence[] array) {
        CharSequence temp[] = new CharSequence[array.length];
        for (int i = 0; i < array.length; i++) {
            temp[i] = array[(array.length - 1) - i];
        }
        return temp;
    }

    public static int[] reverse(int[] array) {
        int temp[] = new int[array.length];
        for (int i = 0; i < array.length; i++) {
            temp[i] = array[(array.length - 1) - i];
        }
        return temp;
    }

    // IndexOf
    public static int indexOf(String[] array, String where) {
        for (int i = 0; i < array.length; i++) {
            if (array[i].equals(where)) {
                return i;
            }
        }
        return -1;
    }

    public static int indexOf(CharSequence[] array, String where) {
        for (int i = 0; i < array.length; i++) {
            if (array[i].equals(where)) {
                return i;
            }
        }
        return -1;
    }

    public static int indexOf(int[] array, int where) {
        for (int i = 0; i < array.length; i++) {
            if (array[i] == where) {
                return i;
            }
        }
        return -1;
    }

    public static String[] trim(String[] array) {
        List<String> list = new ArrayList<>();
        for (String s : array) {
            if (s != null && s.length() > 0) {
                list.add(s);
            }
        }
        return list.toArray(new String[list.size()]);
    }

    public static CharSequence[] trim(CharSequence[] array) {
        List<CharSequence> list = new ArrayList<>();
        for (CharSequence s : array) {
            if (s != null && s.length() > 0) {
                list.add(s);
            }
        }
        return list.toArray(new CharSequence[list.size()]);
    }

    public static Integer[] trim(int[] array) {
        List<Integer> list = new ArrayList<>();
        for (int i : array) {
            if (i > 0) {
                list.add(i);
            }
        }
        return list.toArray(new Integer[list.size()]);
    }
}
