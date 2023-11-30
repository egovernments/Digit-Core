import React from "react";
import { HomeFilled } from "./HomeFilled";

export default {
  tags: ['autodocs'],
  argTypes: {
    className: {
        options: ['custom-class'],
        control: { type: 'check' },
    }
  },
  title: "HomeFilled",
  component: HomeFilled,
};

export const Default = () => <HomeFilled />;
export const Fill = () => <HomeFilled fill="blue" />;
export const Size = () => <HomeFilled height="50" width="50" />;
export const CustomStyle = () => <HomeFilled style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <HomeFilled className="custom-class" />;

export const Clickable = () => <HomeFilled onClick={()=>console.log("clicked")} />;

const Template = (args) => <HomeFilled {...args} />;

export const Playground = Template.bind({});
Playground.args = {
  className: "custom-class",
  style: { border: "3px solid green" }
};
