package com.launchdarkly.client;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import static org.junit.Assert.*;

public class FeatureRepTest {

  private final Variation.TargetRule targetUserOn = new Variation.TargetRule("key", Collections.<JsonElement>singletonList(new JsonPrimitive("targetOn@test.com")));

  private final Variation.TargetRule targetGroupOn = new Variation.TargetRule("groups", Arrays.<JsonElement>asList(new JsonPrimitive("google"), new JsonPrimitive("microsoft")));

  private final Variation.TargetRule targetUserOff = new Variation.TargetRule("key", Collections.<JsonElement>singletonList(new JsonPrimitive("targetOff@test.com")));

  private final Variation.TargetRule targetGroupOff = new Variation.TargetRule("groups", Arrays.<JsonElement>asList(new JsonPrimitive("oracle")));

  private final Variation.TargetRule targetBooleanOff = new Variation.TargetRule("isMember", Arrays.<JsonElement>asList(new JsonPrimitive(true)));

  private final Variation.TargetRule targetIntOn = new Variation.TargetRule("customerRank", Arrays.<JsonElement>asList(new JsonPrimitive(1000), new JsonPrimitive(true), new JsonPrimitive(32.4)));

  private final Variation<Boolean> trueVariation = new Variation.Builder<Boolean>(true, 0)
      .target(targetUserOn)
      .target(targetGroupOn)
      .target(targetIntOn)
      .build();

  private final Variation<Boolean> falseVariation = new Variation.Builder<Boolean>(false, 0)
      .target(targetUserOff)
      .target(targetGroupOff)
      .target(targetBooleanOff)
      .build();

  private final FeatureRep<Boolean> simpleFlag = new FeatureRep.Builder<Boolean>("Sample flag", "sample.flag")
      .on(true)
      .salt("feefifofum")
      .variation(trueVariation)
      .variation(falseVariation)
      .build();

  private final FeatureRep<Boolean> disabledFlag = new FeatureRep.Builder<Boolean>("Sample flag", "sample.flag")
      .on(false)
      .salt("feefifofum")
      .variation(trueVariation)
      .variation(falseVariation)
      .build();

  @Test
  public void testFlagForTargetedUserOff() {
    LDUser user = new LDUser.Builder("targetOff@test.com").build();

    Boolean b = simpleFlag.evaluate(user);

    assertEquals(false, b);
  }

  @Test
  public void testFlagForTargetedUserOn() {
    LDUser user = new LDUser.Builder("targetOn@test.com").build();

    Boolean b = simpleFlag.evaluate(user);

    assertEquals(true, b);
  }

  @Test
  public void testFlagForTargetGroupOn() {
    LDUser user = new LDUser.Builder("targetOther@test.com")
        .custom("groups", Arrays.asList(new JsonPrimitive("google"), new JsonPrimitive("microsoft")))
        .build();

    Boolean b = simpleFlag.evaluate(user);

    assertEquals(true, b);
  }

  @Test
  public void testFlagForTargetGroupOff() {
    LDUser user = new LDUser.Builder("targetOther@test.com")
        .custom("groups", "oracle")
        .build();

    Boolean b = simpleFlag.evaluate(user);

    assertEquals(false, b);
  }

  @Test
  public void testDisabledFlagAlwaysOff() {
    LDUser user = new LDUser("targetOn@test.com");

    Boolean b = disabledFlag.evaluate(user);

    assertEquals(null, b);
  }

  @Test
  public void testFlagWithBooleanCustomAttribute() {
    LDUser user = new LDUser.Builder("randomUser@test.com").custom("isMember", true).build();

    Boolean b = simpleFlag.evaluate(user);
    assertEquals(false, b);
  }

  @Test
  public void testFlagWithIntCustomAttribute() {
    LDUser user = new LDUser.Builder("randomUser@test.com").custom("customerRank", 1000).build();

    Boolean b = simpleFlag.evaluate(user);
    assertEquals(true, b);
  }

}
