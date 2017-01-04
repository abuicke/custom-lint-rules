package com.example;

import com.android.annotations.NonNull;
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
import java.util.Arrays;
import java.util.List;

import lombok.ast.AstVisitor;
import lombok.ast.ForwardingAstVisitor;
import lombok.ast.MethodDeclaration;
import lombok.ast.MethodInvocation;
import lombok.ast.Node;

public class MyAnnotationDetector extends Detector implements Detector.JavaScanner {

  public static final Issue LOGGER_ISSUE = Issue.create(
      "SywLogIsNotUsed",
      "You must use our `SywLog`",
      "Logging should be avoided in production for security and performance reasons."
          + " Therefore, we created a SywLog that wraps all our calls to Logger and disable them for release flavor.",
      Category.USABILITY,
      9,
      Severity.ERROR,
      new Implementation(MyAnnotationDetector.class, Scope.JAVA_FILE_SCOPE));

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
        MethodDeclaration.class);
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
      checkMethod(call);
      return false;
    }

    private void checkMethod(final MethodInvocation call) {
      ResolvedNode resolved = mContext.resolve(call);
      if(resolved instanceof ResolvedMethod) {
        ResolvedMethod method = (ResolvedMethod) resolved;
        final boolean isAndroidLoggingCall = isAnAndroidLoggingMethod(method);
        if(isAndroidLoggingCall) {
          mContext.report(LOGGER_ISSUE, call, mContext.getLocation(call),
              "Logging should be avoided in production for security and performance reasons."
                  + " Therefore, we created a SywLog that wraps all our calls to Logger and disable them for release flavor.");
        }
      }

    }

    private boolean isAnAndroidLoggingMethod(ResolvedMethod method) {
      return method.matches("v") ||
          method.matches("d") ||
          method.matches("i") ||
          method.matches("w") ||
          method.matches("e") ||
          method.matches("wtf");
    }

  }

}