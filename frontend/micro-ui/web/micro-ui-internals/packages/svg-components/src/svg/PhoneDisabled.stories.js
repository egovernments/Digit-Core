import React from "react";
import { PhoneDisabled } from "./PhoneDisabled";

export default {
  tags: ['autodocs'],
  argTypes: {
    className: {
        options: ['custom-class'],
        control: { type: 'check' },
    }
  },
  title: "PhoneDisabled",
  component: PhoneDisabled,
};

export const Default = () => <PhoneDisabled />;
export const Fill = () => <PhoneDisabled fill="blue" />;
export const Size = () => <PhoneDisabled height="50" width="50" />;
export const CustomStyle = () => <PhoneDisabled style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <PhoneDisabled className="custom-class" />;

export const Clickable = () => <PhoneDisabled onClick={()=>console.log("clicked")} />;

const Template = (args) => <PhoneDisabled {...args} />;

export const Playground = Template.bind({});
Playground.args = {
  className: "custom-class",
  style: { border: "3px solid green" }
};
