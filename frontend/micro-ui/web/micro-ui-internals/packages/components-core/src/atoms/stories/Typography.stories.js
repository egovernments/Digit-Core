import React from "react";

export default {
  title: "Typography",
  argTypes: {
    typoName: { control: { type: "select", options: ["text-heading-xl", "text-heading-l","text-heading-m","text-heading-s","text-heading-xs","text-caption-l","text-caption-m","text-caption-s","text-body-l","text-body-s","text-body-xs","text-label","text-link"] } },
  }
};
const Template = (args) => <div className={`typography ${args?.typoName}`} style={args?.style}>Heading XL</div>;

export const Playground = Template.bind({});
Playground.args = {
  typoName: "text-heading-xl",
  style: { color: "inherit" },
};

export const HeadingXL = () => <div className={`typography text-heading-xl`}>Heading XL</div>;
export const HeadingL = () => <div className="typography text-heading-l">Heading L</div>;
export const HeadingM = () => <div className="typography text-heading-m">Heading M</div>;
export const HeadingS = () => <div className="typography text-heading-s">Heading S</div>;
export const HeadingXS = () => <div className="typography text-heading-xs">Heading XS</div>;
export const CaptionL = () => <div className="typography text-caption-l">Caption L</div>;
export const CaptionM = () => <div className="typography text-caption-m">Caption M</div>;
export const CaptionS = () => <div className="typography text-caption-s">Caption S</div>;
export const BodyL = () => <div className="typography text-body-l">Body L</div>;
export const BodyS = () => <div className="typography text-body-s">Body S</div>;
export const BodyXS = () => <div className="typography text-body-xs">Body XS</div>;
export const Label = () => <div className="typography text-label">Label</div>;
export const Link = () => <div className="typography text-link">Link</div>;
