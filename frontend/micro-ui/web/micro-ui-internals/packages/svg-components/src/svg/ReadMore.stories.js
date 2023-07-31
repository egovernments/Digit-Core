import React from "react";
import { ReadMore } from "./ReadMore";

export default {
  tags: ['autodocs'],
  argTypes: {
    className: {
        options: ['custom-class'],
        control: { type: 'check' },
    }
  },
  title: "ReadMore",
  component: ReadMore,
};

export const Default = () => <ReadMore />;
export const Fill = () => <ReadMore fill="blue" />;
export const Size = () => <ReadMore height="50" width="50" />;
export const CustomStyle = () => <ReadMore style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <ReadMore className="custom-class" />;

export const Clickable = () => <ReadMore onClick={()=>console.log("clicked")} />;

const Template = (args) => <ReadMore {...args} />;

export const Playground = Template.bind({});
Playground.args = {
  className: "custom-class",
  style: { border: "3px solid green" }
};
