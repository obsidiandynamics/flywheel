package au.com.williamhill.flywheel.topic;

final class DeleteSubtopic {
  private static final DeleteSubtopic INSTANCE = new DeleteSubtopic();
  
  static DeleteSubtopic instance() { return INSTANCE; }
  
  private DeleteSubtopic() {}

  @Override
  public String toString() {
    return "DeleteSubtopic";
  }
}
