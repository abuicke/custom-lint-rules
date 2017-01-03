package com.example;

import com.android.annotations.NonNull;
import com.android.tools.lint.client.api.JavaParser.ResolvedAnnotation;
import com.android.tools.lint.client.api.JavaParser.ResolvedMethod;
import com.android.tools.lint.client.api.JavaParser.ResolvedNode;
import com.android.tools.lint.detector.api.Category;
import com.android.tools.lint.detector.api.Context;
import com.android.tools.lint.detector.api.Detector;
import com.android.tools.lint.detector.api.Implementation;
import com.android.tools.lint.detector.api.Issue;
import com.android.tools.lint.detector.api.JavaContext;
import com.android.tools.lint.detector.api.Scope;
import com.android.tools.lint.detector.api.Severity;
import com.android.tools.lint.detector.api.Speed;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import lombok.ast.AstVisitor;
import lombok.ast.ConstructorInvocation;
import lombok.ast.ForwardingAstVisitor;
import lombok.ast.MethodInvocation;
import lombok.ast.Node;

public class MyAnnotationDetector extends Detector implements Detector.JavaScanner {

  public static final Issue CAREFUL_NOW_ISSUE = Issue.create(
      "CarefulNow",
      "Be careful when using this method.",
      "This method has special conditions surrounding it's use," +
          " be careful when calling it and refer to its documentation.",
      Category.USABILITY,
      7,
      Severity.WARNING,
      new Implementation(
          MyAnnotationDetector.class,
          Scope.JAVA_FILE_SCOPE));

  private static final String CAREFUL_NOW_ANNOTATION = "com.annotations.CarefulNow";

  private static void checkMethodAnnotation(@NonNull JavaContext context,
                                            @NonNull ResolvedMethod method,
                                            @NonNull Node node,
                                            @NonNull ResolvedAnnotation annotation) {
    String signature = annotation.getSignature();
    if(CAREFUL_NOW_ANNOTATION.equals(signature) || signature.endsWith(".CarefulNow")) {
      checkCarefulNow(context, node, annotation);
    }
  }

  private static void checkCarefulNow(@NonNull JavaContext context,
                                      @NonNull Node node,
                                      @NonNull ResolvedAnnotation annotation) {
    context.report(CAREFUL_NOW_ISSUE, node, context.getLocation(node),
        "This method has special conditions surrounding it's use, " +
            "be careful when calling it and refer to it's documentation.");
  }

  @Override
  public boolean appliesTo(@NonNull Context context, @NonNull File file) {
    return true;
  }

  @NonNull
  @Override
  public Speed getSpeed() {
    return Speed.FAST;
  }

  // ---- Implements JavaScanner ----

  @Override
  public List<Class<? extends Node>> getApplicableNodeTypes() {
    return Arrays.<Class<? extends Node>>asList(
        MethodInvocation.class,
        ConstructorInvocation.class);
  }

  @Override
  public AstVisitor createJavaVisitor(@NonNull JavaContext context) {
    return new CallChecker(context);
  }

  private static class CallChecker extends ForwardingAstVisitor {

    private final JavaContext mContext;

    public CallChecker(JavaContext context) {
      mContext = context;
    }

    @Override
    public boolean visitMethodInvocation(@NonNull MethodInvocation call) {
      ResolvedNode resolved = mContext.resolve(call);
      if(resolved instanceof ResolvedMethod) {
        ResolvedMethod method = (ResolvedMethod) resolved;
        checkCall(call, method);
      }

      return false;
    }

    @Override
    public boolean visitConstructorInvocation(@NonNull ConstructorInvocation call) {
      ResolvedNode resolved = mContext.resolve(call);
      if(resolved instanceof ResolvedMethod) {
        ResolvedMethod method = (ResolvedMethod) resolved;
        checkCall(call, method);
      }

      return false;
    }

    private void checkCall(@NonNull Node call, ResolvedMethod method) {
      Iterable<ResolvedAnnotation> annotations = method.getAnnotations();
      annotations = filterRelevantAnnotations(annotations);
      for(ResolvedAnnotation annotation : annotations) {
        checkMethodAnnotation(mContext, method, call, annotation);
      }
    }

    private Iterable<ResolvedAnnotation> filterRelevantAnnotations(Iterable<ResolvedAnnotation> resolvedAnnotationsIn) {
      List<ResolvedAnnotation> resolvedAnnotationsOut = new ArrayList<>();
      for(ResolvedAnnotation resolvedAnnotation : resolvedAnnotationsIn) {
        if(resolvedAnnotation.matches(CAREFUL_NOW_ANNOTATION)) {
          resolvedAnnotationsOut.add(resolvedAnnotation);
        }
      }

      return resolvedAnnotationsOut;
    }

  }

}