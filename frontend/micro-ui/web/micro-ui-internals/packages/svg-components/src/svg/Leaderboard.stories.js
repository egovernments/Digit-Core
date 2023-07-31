import React from "react";
import { Leaderboard } from "./Leaderboard";

export default {
  tags: ['autodocs'],
  argTypes: {
    className: {
        options: ['custom-class'],
        control: { type: 'check' },
    }
  },
  title: "Leaderboard",
  component: Leaderboard,
};

export const Default = () => <Leaderboard />;
export const Fill = () => <Leaderboard fill="blue" />;
export const Size = () => <Leaderboard height="50" width="50" />;
export const CustomStyle = () => <Leaderboard style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <Leaderboard className="custom-class" />;

export const Clickable = () => <Leaderboard onClick={()=>console.log("clicked")} />;

const Template = (args) => <Leaderboard {...args} />;

export const Playground = Template.bind({});
Playground.args = {
  className: "custom-class",
  style: { border: "3px solid green" }
};
