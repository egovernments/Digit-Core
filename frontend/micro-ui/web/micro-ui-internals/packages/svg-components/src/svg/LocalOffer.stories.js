import React from "react";
import { LocalOffer } from "./LocalOffer";

export default {
  tags: ['autodocs'],
  argTypes: {
    className: {
        options: ['custom-class'],
        control: { type: 'check' },
    }
  },
  title: "LocalOffer",
  component: LocalOffer,
};

export const Default = () => <LocalOffer />;
export const Fill = () => <LocalOffer fill="blue" />;
export const Size = () => <LocalOffer height="50" width="50" />;
export const CustomStyle = () => <LocalOffer style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <LocalOffer className="custom-class" />;

export const Clickable = () => <LocalOffer onClick={()=>console.log("clicked")} />;

const Template = (args) => <LocalOffer {...args} />;

export const Playground = Template.bind({});
Playground.args = {
  className: "custom-class",
  style: { border: "3px solid green" }
};
