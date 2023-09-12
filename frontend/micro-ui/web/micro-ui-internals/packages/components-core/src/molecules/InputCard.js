import React from "react";
import PropTypes from "prop-types";

import Card from "../atoms/Card";
import CardHeader from "../atoms/CardHeader";
import CardText from "../atoms/CardText";
import Button from "../atoms/Button";
import CardCaption from "../atoms/CardCaption";
import TextInput from "../atoms/TextInput";
import ActionLinks from "../atoms/ActionLinks";

const InputCard = ({
  t,
  children,
  texts = {},
  submit = false,
  inputs = [],
  inputRef,
  onNext,
  onSkip,
  isDisable,
  onAdd,
  isMultipleAllow = false,
  cardStyle = {},
}) => {
  // TODO: inputs handle
  return (
    <Card style={cardStyle}>
      {texts.headerCaption && <CardCaption>{t(texts.headerCaption)}</CardCaption>}
      {texts?.header && <CardHeader>{t(texts.header)}</CardHeader>}
      {texts?.cardText && <CardText>{t(texts.cardText)}</CardText>}
      {children}
      {texts.submitBarLabel ? <Button disabled={isDisable} submit={submit} label={t(texts.submitBarLabel)} onClick={onNext} /> : null}
      {texts.skipLabel ? <CardText> {t(texts.skipLabel)} </CardText> : null}
      {texts.skipText ? <ActionLinks label={t(texts.skipText)} onClick={onSkip} /> : null}
      {isMultipleAllow && texts.addMultipleText ? <ActionLinks label={t(texts.addMultipleText)} onClick={onAdd} /> : null}
    </Card>
  );
};

InputCard.propTypes = {
  text: PropTypes.object,
  submit: PropTypes.bool,
  onNext: PropTypes.func,
  onSkip: PropTypes.func,
  onAdd: PropTypes.func,
  t: PropTypes.func,
};

InputCard.defaultProps = {
  texts: {},
  submit: false,
  onNext: undefined,
  onSkip: undefined,
  onAdd: undefined,
  t: (value) => value,
};

export default InputCard;
