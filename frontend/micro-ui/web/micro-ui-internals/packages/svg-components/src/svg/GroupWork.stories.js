import React from "react";
import { GroupWork } from "./GroupWork";

export default {
  tags: ['autodocs'],
  argTypes: {
    className: {
        options: ['custom-class'],
        control: { type: 'check' },
    }
  },
  title: "GroupWork",
  component: GroupWork,
};

export const Default = () => <GroupWork />;
export const Fill = () => <GroupWork fill="blue" />;
export const Size = () => <GroupWork height="50" width="50" />;
export const CustomStyle = () => <GroupWork style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <GroupWork className="custom-class" />;

export const Clickable = () => <GroupWork onClick={()=>console.log("clicked")} />;

const Template = (args) => <GroupWork {...args} />;

export const Playground = Template.bind({});
Playground.args = {
  className: "custom-class",
  style: { border: "3px solid green" }
};
