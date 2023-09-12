import React from "react";
import Card from "./Card";
import Button from "./Button";

const PageBasedInput = ({ children, texts, onSubmit }) => {
  return (
    <div className="digit-page-based-input-wrapper">
      <Card>
        {children}
        <Button className="digit-page-based-submit-bar" label={texts.submitBarLabel} onClick={onSubmit} />
      </Card>
      <div className="digit-submit-bar-container">
        <Button variation="digit-submit-bar" label={texts.submitBarLabel} onClick={onSubmit} />
      </div>
    </div>
  );
};

export default PageBasedInput;
