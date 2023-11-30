import React from "react";
import { LabelImportant } from "./LabelImportant";

export default {
  tags: ['autodocs'],
  argTypes: {
    className: {
        options: ['custom-class'],
        control: { type: 'check' },
    }
  },
  title: "LabelImportant",
  component: LabelImportant,
};

export const Default = () => <LabelImportant />;
export const Fill = () => <LabelImportant fill="blue" />;
export const Size = () => <LabelImportant height="50" width="50" />;
export const CustomStyle = () => <LabelImportant style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <LabelImportant className="custom-class" />;

export const Clickable = () => <LabelImportant onClick={()=>console.log("clicked")} />;

const Template = (args) => <LabelImportant {...args} />;

export const Playground = Template.bind({});
Playground.args = {
  className: "custom-class",
  style: { border: "3px solid green" }
};
