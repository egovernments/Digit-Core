import React from "react";
import Card from "../atoms/Card";
import Button from "./Button";

const PageBasedInput = ({ children, texts, onSubmit }) => {
  return (
    <div className="PageBasedInputWrapper PageBased">
      <Card>
        {children}
        <Button className="SubmitBarInCardInDesktopView" label={texts.submitBarLabel} onClick={onSubmit} />
      </Card>
      <div className="SubmitBar">
        <Button label={texts.submitBarLabel} onClick={onSubmit} />
      </div>
    </div>
  );
};

export default PageBasedInput;
