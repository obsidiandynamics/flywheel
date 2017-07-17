package au.com.williamhill.flywheel.edge;

import au.com.williamhill.flywheel.frame.*;

public class TopicLambdaListener implements TopicListener {
  @FunctionalInterface public interface OnOpen {
    void onOpen(EdgeNexus nexus);
  }
  
  @FunctionalInterface public interface OnClose {
    void onClose(EdgeNexus nexus);
  }
  
  @FunctionalInterface public interface OnBind {
    void onBind(EdgeNexus nexus, BindFrame bind, BindResponseFrame bindRes);
  }
  
  @FunctionalInterface public interface OnPublishText {
    void onPublish(EdgeNexus nexus, PublishTextFrame pub);
  }
  
  @FunctionalInterface public interface OnPublishBinary {
    void onPublish(EdgeNexus nexus, PublishBinaryFrame pub);
  }
  
  private OnOpen onOpen;
  
  private OnClose onClose;
  
  private OnBind onBind;
  
  private OnPublishText onPublishText;
  
  private OnPublishBinary onPublishBinary;
  
  @Override public void onOpen(EdgeNexus nexus) {
    if (onOpen != null) onOpen.onOpen(nexus);
  }
  
  @Override public void onClose(EdgeNexus nexus) {
    if (onClose != null) onClose.onClose(nexus);
  }
  
  @Override public void onBind(EdgeNexus nexus, BindFrame bind, BindResponseFrame bindRes) {
    if (onBind != null) onBind.onBind(nexus, bind, bindRes);
  }
  
  @Override public void onPublish(EdgeNexus nexus, PublishTextFrame pub) {
    if (onPublishText != null) onPublishText.onPublish(nexus, pub);
  }
  
  @Override public void onPublish(EdgeNexus nexus, PublishBinaryFrame pub) {
    if (onPublishBinary != null) onPublishBinary.onPublish(nexus, pub);
  }
  
  public final TopicLambdaListener onOpen(OnOpen onOpen) {
    this.onOpen = onOpen;
    return this;
  }

  public final TopicLambdaListener onClose(OnClose onClose) {
    this.onClose = onClose;
    return this;
  }

  public final TopicLambdaListener onBind(OnBind onBind) {
    this.onBind = onBind;
    return this;
  }

  public final TopicLambdaListener onPublishText(OnPublishText onPublishText) {
    this.onPublishText = onPublishText;
    return this;
  }

  public final TopicLambdaListener onPublishBinary(OnPublishBinary onPublishBinary) {
    this.onPublishBinary = onPublishBinary;
    return this;
  }
}
