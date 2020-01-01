/*----------------------------------------------------------------------------*/
/* Copyright (c) 2016-2019 FIRST. All Rights Reserved.                        */
/* Open Source Software - may be modified and shared by FRC teams. The code   */
/* must be accompanied by the FIRST BSD license file in the root directory of */
/* the project.                                                               */
/*----------------------------------------------------------------------------*/

package edu.wpi.first.hal;

/**
 * Dummy HAL to shim usage reporting.
 */
@SuppressWarnings({"AbbreviationAsWordInName", "MethodName", "PMD.TooManyMethods"})
public final class HAL {
  private HAL() {
  }

  public static void report(int resource, int instanceNumber) {
  }

  public static void report(int resource, int instanceNumber, int context) {
  }

  public static int report(int resource, int instanceNumber, int context, String feature) {
    return 0;
  }
}
