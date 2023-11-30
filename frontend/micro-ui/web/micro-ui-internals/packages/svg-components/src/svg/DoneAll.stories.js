import React from "react";
import { DoneAll } from "./DoneAll";

export default {
  tags: ['autodocs'],
  argTypes: {
    className: {
        options: ['custom-class'],
        control: { type: 'check' },
    }
  },
  title: "DoneAll",
  component: DoneAll,
};

export const Default = () => <DoneAll />;
export const Fill = () => <DoneAll fill="blue" />;
export const Size = () => <DoneAll height="50" width="50" />;
export const CustomStyle = () => <DoneAll style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <DoneAll className="custom-class" />;

export const Clickable = () => <DoneAll onClick={()=>console.log("clicked")} />;

const Template = (args) => <DoneAll {...args} />;

export const Playground = Template.bind({});
Playground.args = {
  className: "custom-class",
  style: { border: "3px solid green" }
};
