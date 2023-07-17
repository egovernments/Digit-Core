import React from "react";
import { Fingerprint } from "./Fingerprint";

export default {
  tags: ['autodocs'],
  argTypes: {
    className: {
        options: ['custom-class'],
        control: { type: 'check' },
    }
  },
  title: "Fingerprint",
  component: Fingerprint,
};

export const Default = () => <Fingerprint />;
export const Fill = () => <Fingerprint fill="blue" />;
export const Size = () => <Fingerprint height="50" width="50" />;
export const CustomStyle = () => <Fingerprint style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <Fingerprint className="custom-class" />;

export const Clickable = () => <Fingerprint onClick={()=>console.log("clicked")} />;

const Template = (args) => <Fingerprint {...args} />;

export const Playground = Template.bind({});
Playground.args = {
  className: "custom-class",
  style: { border: "3px solid green" }
};
